package mima

import argonaut.CodecJson
import httpz.{Error, JsonToString}

import scala.concurrent.{ExecutionContext, Future}

final case class MavenSearch(response: MavenSearch.Response) extends JsonToString[MavenSearch]

object MavenSearch {
  implicit val codecJson: CodecJson[MavenSearch] =
    CodecJson.casecodec1(apply, unapply)(
      "response"
    )

  def searchByGroupId(groupId: String): Future[Either[Error, List[String]]] =
    Future {
      import httpz._
      import httpz.native._

      val req = Request(
        url = "http://search.maven.org/solrsearch/select",
        params = Map(
          "q" -> s"g:$groupId",
          "rows" -> "256",
          "wt" -> "json"
        )
      )

      Core
        .json[MavenSearch](req)
        .interpret
        .map(
          _.response.docs.withFilter(_.hasJar).map(_.artifactId).sorted
        )
        .toEither
    }(ExecutionContext.global)

  final case class Jar(
      artifactId: String,
      text: List[String]
  ) extends JsonToString[Jar] {
    def hasJar: Boolean = text.contains(".jar")
  }

  object Jar {
    implicit val codecJson: CodecJson[Jar] =
      CodecJson.casecodec2(apply, unapply)(
        "a",
        "text"
      )
  }

  final case class Response(
      docs: List[Jar]
  ) extends JsonToString[Response]

  object Response {
    implicit val responseCodecJson: CodecJson[Response] =
      CodecJson.casecodec1(apply, unapply)(
        "docs"
      )
  }
}
