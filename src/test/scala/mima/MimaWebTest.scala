package mima

import unfiltered.jetty.Server

import scalaj.http._
import org.scalatest.funspec.AnyFunSpec

class MimaWebTest extends AnyFunSpec {
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
    """abstract method ToAssociativeOps(java.lang.Object,scalaz.Associative)scalaz.syntax.AssociativeOps in trait scalaz.syntax.ToAssociativeOps is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |abstract method ToAssociativeOpsUnapply(java.lang.Object,scalaz.Unapply2)scalaz.syntax.AssociativeOps in trait scalaz.syntax.ToAssociativeOps0 is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |abstract method ToAssociativeVFromKleisliLike(java.lang.Object,scalaz.Associative)scalaz.syntax.AssociativeOps in trait scalaz.syntax.ToAssociativeOps is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |abstract method ToProChoiceOps(java.lang.Object,scalaz.ProChoice)scalaz.syntax.ProChoiceOps in trait scalaz.syntax.ToProChoiceOps is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |abstract method ToProChoiceOpsUnapply(java.lang.Object,scalaz.Unapply2)scalaz.syntax.ProChoiceOps in trait scalaz.syntax.ToProChoiceOps0 is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |abstract method ToProChoiceVFromKleisliLike(java.lang.Object,scalaz.ProChoice)scalaz.syntax.ProChoiceOps in trait scalaz.syntax.ToProChoiceOps is inherited by class ToTypeClassOps in scalaz-core_2.11-7.1.1.jar version.
    |class scalaz.Monoid#ApplicativeMonoid#class is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today
    |class scalaz.Semigroup#ApplySemigroup#class is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today
    |class scalaz.std.Tuple1Cozip#class is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today
    |class scalaz.std.Tuple1Functor#class is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today
    |class scalaz.std.Tuple1Monad#class is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today
    |private trait scalaz.Monoid#ApplicativeMonoid is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today (scalaz.Monoid#ApplicativeMonoid escaped through scalaz.Monoid#ApplicativeMonoid##anonfun#1.this)
    |private trait scalaz.std.Tuple1Cozip is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today (scalaz.std.Tuple1Cozip escaped as a parent of class scalaz.std.TupleInstances1##anon#57)
    |private trait scalaz.std.Tuple1Functor is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today (scalaz.std.Tuple1Functor escaped as a parent of class scalaz.std.TupleInstances0##anon#1)
    |private trait scalaz.std.Tuple1Monad is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today (scalaz.std.Tuple1Monad escaped as a parent of class scalaz.std.TupleInstances0##anon#1)
    |private[..] class scalaz.Free#Return is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today (scalaz.Free#Return escaped through scalaz.TrampolineInstances##anon#2.cojoin)
    |private[..] trait scalaz.Semigroup#ApplySemigroup is not part of the API in scalaz-core_2.11-7.1.1.jar version, so a later change to it will no longer be reported, though it would break clients using it today (scalaz.Semigroup#ApplySemigroup escaped as a parent of trait scalaz.Monoid#ApplicativeMonoid, through scalaz.Monoid#ApplicativeMonoid##anonfun#1.this)""".stripMargin

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
