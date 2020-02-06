package internal.proj

import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import cats.syntax.functor._
import internal.proj.routes.ApplicationRouter
import internal.proj.services.GitHubService
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Application extends IOApp {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  private val token = Option(System.getenv("GH_TOKEN"))

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      client <- BlazeClientBuilder[IO](global).resource
      server <- BlazeServerBuilder[IO]
        .withResponseHeaderTimeout(160.seconds)
        .withIdleTimeout(170.seconds)
        .bindHttp(8080, "localhost")
        .withHttpApp(new ApplicationRouter[IO](new GitHubService[IO](client, token)).routes)
        .resource
    } yield server)
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}
