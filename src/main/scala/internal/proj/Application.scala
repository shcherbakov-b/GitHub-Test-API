package internal.proj

import cats.effect.{ExitCode, IO, IOApp}
import internal.proj.services.GitHubService
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.blaze._
import org.http4s.client._
import internal.proj.codecs._
import internal.proj.models.{Contributor, Repo}
import org.http4s.circe._

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends IOApp {

  implicit val decoder = jsonOf[IO, List[Repo]]
  implicit val decoder2 = jsonOf[IO, List[Contributor]]

  override def run(args: List[String]): IO[ExitCode] = BlazeClientBuilder[IO](global)
    .resource.use { client =>
    val service = new GitHubService(client)
    for {
      repos <- service.repos("wserverv")
      contributors <- service.allContributors(repos)

    } yield {
      contributors.foreach(println)
      ExitCode.Success
    }
  }
    // use `client` here and return an `IO`.
    // the client will be acquired and shut down
    // automatically each time the `IO` is run.
}
