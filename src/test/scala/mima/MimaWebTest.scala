package mima

import unfiltered.jetty.Server
import org.scalatest.funspec.AnyFunSpec

import java.net.URI
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.time.Duration

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

  private def createRequest(uri: String) = HttpRequest
    .newBuilder()
    .uri(URI.create(uri))
    .timeout(Duration.ofSeconds(30))
    .build()

  it("MimaWeb") {
    withServer { port =>
      val client = HttpClient.newHttpClient()
      def get(uri: String): HttpResponse[String] =
        client.send(createRequest(uri), BodyHandlers.ofString(StandardCharsets.UTF_8))

      // https://github.com/scalaz/scalaz/issues/1199
      val response = get(s"http://localhost:$port/org.scalaz/scalaz-core_2.11?previous=7.1.0&current=7.1.1")
      assert(response.statusCode == 200)
      assert(response.body == expect)

      val res1 = get(s"http://localhost:$port/org.scalaz")
      assert(res1.statusCode == 200, res1.body)

      val res2 = get(s"http://localhost:$port/org.scalaz/scalaz-core_2.12")
      assert(res2.statusCode == 200, res2.body)

      val res3 = get(s"http://localhost:$port/org.scalaz/scalaz-core_2.12?current=7.2.8")
      assert(res3.statusCode == 200, res3.body)
      assert(res2.body.length > res3.body.length)
    }
  }
}
