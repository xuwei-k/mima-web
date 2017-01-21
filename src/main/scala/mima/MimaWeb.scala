package mima

import java.io.File

import com.typesafe.tools.mima.cli.Main
import sbt.io.IO
import unfiltered.filter.Plan.Intent
import unfiltered.jetty.{Server, SocketPortBinding}
import unfiltered.request._
import unfiltered.response.{Ok, ResponseString, Status}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scalaj.http.HttpOptions
import scalaz.Nondeterminism

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

  private[this] val cache: Cache[Library, Array[Byte], Int] =
    new Cache(download)

  class StrParam(val name: String) {
    def unapply(p: Params.Map): Option[String] =
      p.get(name).flatMap(_.headOption)
  }

  private[this] val Current = new StrParam("current")
  private[this] val Previous = new StrParam("previous")

  private[this] val instance: Nondeterminism[Future] =
    scalaz.std.scalaFuture.futureInstance(ExecutionContext.global)

  override val intent: Intent = {
    case GET(Path(Seg(groupId :: artifactId :: Nil)) & Params(Previous(p) & Current(c))) =>
      val previous = Library(groupId, artifactId, p)
      val current = Library(groupId, artifactId, c)
      val result = instance.mapBoth(cache.get(previous), cache.get(current)) {
        case (Right(x), Right(y)) =>
          val problems = IO.withTemporaryDirectory { dir =>
            val p = new File(dir, previous.name)
            val c = new File(dir, current.name)
            IO.write(p, x)
            IO.write(c, y)
            runMima(p.getAbsolutePath, c.getAbsolutePath)
          }
          val res = if (problems.isEmpty) {
            "Found 0 binary incompatibilities"
          } else {
            problems.map(_.description(current.name)).sorted.mkString("\n")
          }
          println(res)
          Ok ~> ResponseString(res)
        case (Left(code), _) =>
          Status(code) ~> ResponseString(s"status = $code. error while downloading ${current.mavenCentralURL}")
        case (_, Left(code)) =>
          Status(code) ~> ResponseString(s"status = $code. error while downloading ${current.mavenCentralURL}")
      }
      Await.result(result, 29.seconds)
  }

  def main(args: Array[String]): Unit = {
    val port = util.Try { System.getenv("PORT").toInt }.getOrElse(8080)
    val binding = SocketPortBinding(port, "0.0.0.0")
    Server.portBinding(binding).plan(this).run()
  }

  private def runMima(previous: String, current: String) = {
    val m = new Main(Nil)
    val mima = m.makeMima
    mima.collectProblems(previous, current)
  }
}
