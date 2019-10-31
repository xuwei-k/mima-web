package mima

import java.io.File

import com.typesafe.tools.mima.core.Problem
import com.typesafe.tools.mima.core.util.log.Logging
import sbt.io.IO
import unfiltered.filter.Plan.Intent
import unfiltered.jetty.{Server, SocketPortBinding}
import unfiltered.request._
import unfiltered.response.{Html5, InternalServerError, Ok, ResponseString, Status}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scala.xml.{Elem, XML}
import scalaj.http.HttpOptions
import scalaz.Nondeterminism

import scala.collection.mutable.ListBuffer

object MimaWeb extends unfiltered.filter.Plan {
  val defaultOptions: Seq[HttpOptions.HttpOption] = Seq(
    _.setConnectTimeout(30000),
    _.setReadTimeout(30000)
  )

  private[this] val download: Library => Future[Either[Int, Array[Byte]]] = { lib =>
    Future {
      val req = scalaj.http.Http(lib.mavenCentralURL).options(defaultOptions)
      println(s"downloading from ${lib.mavenCentralURL}")
      val res = req.asBytes
      println("status = " + res.code + " " + lib.mavenCentralURL)
      if (res.code == 200) {
        Right(res.body)
      } else {
        Left(res.code)
      }
    }(ExecutionContext.global)
  }

  private[this] val cacheJars: Cache[Library, Array[Byte], Int] =
    new Cache(download)
  private[this] val cacheArtifacts: Cache[String, List[String], httpz.Error] =
    new Cache(MavenSearch.searchByGroupId)
  private[this] val cacheVersions: Cache[(String, String), List[String], String] =
    new Cache({ x =>
      Future(versions(Library.MavenCentral, x._1, x._2))(ExecutionContext.global)
    })

  class StrParam(val name: String) {
    def unapply(p: Params.Map): Option[String] =
      p.get(name).flatMap(_.headOption)
  }

  private[this] val Current = new StrParam("current")
  private[this] val Previous = new StrParam("previous")

  private[this] val instance: Nondeterminism[Future] =
    scalaz.std.scalaFuture.futureInstance(ExecutionContext.global)

  private def returnHtml(x: Elem) = Html5(
    <html>
      <head>
        <title>migration-manager web API</title>
        <meta name="robots" content="noindex,nofollow" />
      </head>
      <body><div>{x}</div></body>
    </html>
  )

  private final val baseURL = "https://migration-manager.herokuapp.com/"

  override val intent: Intent = {
    case GET(Path(Seg(groupId :: Nil))) =>
      Await.result(cacheArtifacts.get(groupId), 29.seconds) match {
        case Left(e) =>
          InternalServerError ~> ResponseString(e.toString)
        case Right(artifacts) =>
          returnHtml(
            <div>{
              artifacts.map { a =>
                <li><a href={s"${baseURL}$groupId/${a}"}>{a}</a></li>
              }
            }</div>
          )
      }

    case GET(Path(Seg(groupId :: artifactId :: Nil)) & Params(param @ Previous(p) & Current(c))) =>
      val debug = param.get("debug").exists(_.contains("true"))
      val previous = Library(groupId, artifactId, p)
      val current = Library(groupId, artifactId, c)
      val result = instance.mapBoth(cacheJars.get(previous), cacheJars.get(current)) {
        case (Right(x), Right(y)) =>
          val (problems, log) = IO.withTemporaryDirectory { dir =>
            val p = new File(dir, previous.name)
            val c = new File(dir, current.name)
            IO.write(p, x)
            IO.write(c, y)
            runMima(p.getAbsolutePath, c.getAbsolutePath)
          }
          val res0 = if (problems.isEmpty) {
            "Found 0 binary incompatibilities"
          } else {
            problems.map(_.description(current.name)).sorted.mkString("\n")
          }
          println(res0)
          val res = if (debug) {
            res0 + "\n" + log.mkString("\n")
          } else {
            res0
          }
          Ok ~> ResponseString(res)
        case (Left(code), _) =>
          Status(code) ~> ResponseString(s"status = $code. error while downloading ${current.mavenCentralURL}")
        case (_, Left(code)) =>
          Status(code) ~> ResponseString(s"status = $code. error while downloading ${current.mavenCentralURL}")
      }
      Await.result(result, 29.seconds)

    case GET(Path(Seg(groupId :: artifactId :: Nil))) & Params(params) =>
      Await.result(cacheVersions.get((groupId, artifactId)), 29.seconds) match {
        case Right(xs) =>
          def v(p: StrParam) = {
            p.unapply(params) match {
              case Some(x) => x :: Nil
              case None    => xs
            }
          }

          Ok ~> returnHtml(<div>
            {
            for {
              previous <- v(Previous)
              current <- v(Current)
              if previous != current
            } yield {
              <li>
                <a target="_brank" href={s"$baseURL$groupId/$artifactId?previous=$previous&current=$current"}>
                  {s"previous=$previous current=$current"}
                </a>
              </li>
            }
          }
          </div>)
        case Left(error) =>
          InternalServerError ~> ResponseString(error)
      }

    case GET(Path(Seg(Nil))) =>
      val url = "https://github.com/xuwei-k/mima-web"
      returnHtml(<h1><a href={url}>{url}</a></h1>)
  }

  private def versions(baseUrl: String, groupId: String, artifactId: String): Either[String, List[String]] =
    metadataXml(baseUrl, groupId, artifactId).right.map { x =>
      (x \\ "version").map(_.text).toList.sorted
    }

  private def metadataXml(baseUrl: String, groupId: String, artifactId: String): Either[String, Elem] =
    try {
      val url = s"$baseUrl${groupId.replace('.', '/')}/$artifactId/maven-metadata.xml"
      println(s"downloading $url")
      Right(XML.load(url))
    } catch {
      case e: _root_.org.xml.sax.SAXParseException => // ignore
        Left(e.toString)
      case NonFatal(e) =>
        e.printStackTrace()
        Left(e.toString)
    }

  def main(args: Array[String]): Unit = {
    val port = util.Try { System.getenv("PORT").toInt }.getOrElse(8080)
    val binding = SocketPortBinding(port, "0.0.0.0")
    Server.portBinding(binding).plan(this).run()
  }

  private def runMima(previous: String, current: String): (List[Problem], List[String]) = {
    val buf = new ListBuffer[String]
    val logger = new Logging {
      override def debugLog(str: String): Unit = {}
      override def error(str: String): Unit = {
        val x = s"[error] $str"
        println(x)
        buf += x
      }
      override def info(str: String): Unit = {
        val x = s"[info] $str"
        println(x)
        buf += x
      }
      override def warn(str: String): Unit = {
        val x = s"[warn] $str"
        println(x)
        buf += x
      }
    }
    com.typesafe.tools.mima.core.Config.setup("mima-web", Array.empty)
    val classpath = com.typesafe.tools.mima.core.reporterClassPath("")
    val m = new com.typesafe.tools.mima.lib.MiMaLib(classpath, logger)
    val problems = m.collectProblems(previous, current)
    (problems, buf.toList)
  }
}
