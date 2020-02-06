package internal.proj.utils

import cats.data.NonEmptyList
import internal.proj.routes.GitHubRoutes
import org.http4s.{Headers, Uri}
import org.http4s.headers.{Link, LinkValue}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GitHubPageExtractorSpec extends AnyFlatSpec with Matchers {

  val lastPage: Uri = Uri.unsafeFromString(GitHubRoutes.reposRoute("octokit")).withQueryParam("page", "10")
  val firstPage: Uri = Uri.unsafeFromString(GitHubRoutes.reposRoute("octokit")).withQueryParam("page", "1")

  val correctHeaders: Headers = Headers.of(
    Link(
      NonEmptyList(
        LinkValue(uri = lastPage, rel = Some(GitHubPageExtractor.MetaLast)),
        Nil)
    )
  )

  val emptyHeaders: Headers = Headers.empty

  val incorrectHeaders: Headers = Headers.of(
    Link(
      NonEmptyList(
        LinkValue(uri = firstPage, rel = Some(GitHubPageExtractor.MetaLast)),
        Nil
      )
    )
  )

  "Page extractor" should "return 9 uri" in {
    GitHubPageExtractor.next(correctHeaders).size should be(9)
  }

  it should "return empty list if headers are empty" in {
    GitHubPageExtractor.next(emptyHeaders) should be(List.empty)
  }

  it should "return empty list if we get broken link" in {
    GitHubPageExtractor.next(incorrectHeaders) should be(List.empty)
  }

}
