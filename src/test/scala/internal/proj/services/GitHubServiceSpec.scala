package internal.proj.services

import cats.effect.{ContextShift, IO, Resource}
import internal.proj.codecs._
import internal.proj.errors.DomainError.OrganizationNotFound
import internal.proj.models._
import io.circe.syntax._
import fs2._
import org.http4s.Response
import org.http4s.client.Client
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class GitHubServiceSpec extends AnyFlatSpec with Matchers {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  "Service" should "throw an OrganizationNotFound exception" in {
    the[OrganizationNotFound] thrownBy new GitHubService[IO](Client[IO](_ => Resource.pure(Response.notFound)), None)
      .repos("no-matter").unsafeRunSync()
  }

  it should "return list of repositories" in {
    val expectedListOfRepos = List(Repo("first", "no-matter"), Repo("second", "no-matter"))
    new GitHubService[IO](Client[IO](_ => Resource.pure(Response(body = Stream.emits[IO, Byte](
      expectedListOfRepos
        .asJson
        .noSpaces
        .getBytes)))), None)
      .repos("no-matter").unsafeRunSync() should be(expectedListOfRepos)
  }

  it should "return sorted list of contributors" in {
    val json =
      """[{
        |   "login" : "FreeGuy",
        |   "contributions" : 2
        |  },
        |  {
        |    "login" : "Cooler",
        |    "contributions" : 10
        |  },
        |  {
        |    "login" : "My",
        |    "contributions" : 5
        |  },
        |  {
        |    "login" : "FreeGuy",
        |    "contributions" : 15
        |  },
        |  {
        |    "login" : "My",
        |    "contributions" : 7
        |  }]""".stripMargin
    new GitHubService[IO](Client[IO](_ => Resource.pure(Response(body = Stream.emits[IO, Byte](
      json.getBytes)))), None)
      .allContributors(List(Repo("empty", "no-matter")))
      .unsafeRunSync() should be(List(Contributor("FreeGuy", 17), Contributor("My", 12), Contributor("Cooler", 10)))

  }

}
