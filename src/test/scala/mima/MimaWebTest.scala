package mima

import org.scalatest.FunSpec
import unfiltered.jetty.Server

import scalaj.http._

class MimaWebTest extends FunSpec {

  def withServer[A](action: Int => A): A = {
    val server = Server.anylocal
    server.plan(MimaWeb).start()
    try {
      action(server.ports.headOption.getOrElse(sys.error("ports empty!?")))
    } finally {
      server.stop()
    }
  }

  val expect =
    """method ToAssociativeOps(java.lang.Object,scalaz.Associative)scalaz.syntax.AssociativeOps in trait scalaz.syntax.ToAssociativeOps is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |method ToAssociativeOpsUnapply(java.lang.Object,scalaz.Unapply2)scalaz.syntax.AssociativeOps in trait scalaz.syntax.ToAssociativeOps0 is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |method ToAssociativeVFromKleisliLike(java.lang.Object,scalaz.Associative)scalaz.syntax.AssociativeOps in trait scalaz.syntax.ToAssociativeOps is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |method ToProChoiceOps(java.lang.Object,scalaz.ProChoice)scalaz.syntax.ProChoiceOps in trait scalaz.syntax.ToProChoiceOps is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |method ToProChoiceOpsUnapply(java.lang.Object,scalaz.Unapply2)scalaz.syntax.ProChoiceOps in trait scalaz.syntax.ToProChoiceOps0 is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |method ToProChoiceVFromKleisliLike(java.lang.Object,scalaz.ProChoice)scalaz.syntax.ProChoiceOps in trait scalaz.syntax.ToProChoiceOps is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.""".stripMargin

  it("MimaWeb") {
    withServer { port =>
      // https://github.com/scalaz/scalaz/issues/1199
      val request =
        Http(s"http://localhost:$port/org.scalaz/scalaz-core_2.11")
          .param("previous", "7.1.0")
          .param("current", "7.1.1")
          .options(MimaWeb.defaultOptions)
      val response = request.asString
      assert(response.code == 200)
      assert(response.body == expect)

      val artifacts = Http(s"http://localhost:$port/org.scalaz")
      val res1 = artifacts.asString
      assert(res1.code == 200, res1.body)

      val versions1 = Http(s"http://localhost:$port/org.scalaz/scalaz-core_2.12")
      val res2 = versions1.asString
      assert(res2.code == 200, res2.body)

      val versions2 = Http(s"http://localhost:$port/org.scalaz/scalaz-core_2.12?current=7.2.8")
      val res3 = versions2.asString
      assert(res3.code == 200, res3.body)
      assert(res2.body.length > res3.body.length)
    }
  }
}
