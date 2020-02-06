package internal.proj.routes

import cats.data.{EitherT, Kleisli}
import cats.effect.Async
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import internal.proj.codecs._
import internal.proj.models.Contributor
import internal.proj.services.SearchService
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.syntax.kleisli._
import org.http4s.{HttpRoutes, Request, Response}
import org.slf4j.LoggerFactory

class ApplicationRouter[F[_] : Async](service: SearchService[F]) extends Http4sDsl[F] {

  private val logger = LoggerFactory.getLogger(getClass)

  val routes: Kleisli[F, Request[F], Response[F]] = HttpRoutes.of[F] {
    case GET -> Root / "org" / name / "contributors" =>
      (for {
        _ <- EitherT.pure[F, Throwable](logger.info(s"Received request to collect contributors of organization - $name"))
        repos <- EitherT(service.repos(name).attempt)
        _ <- EitherT.pure[F, Throwable](logger.info("Total repos amount - " + repos.size))
        contributors <- EitherT.liftF[F, Throwable, List[Contributor]](service.allContributors(repos))
        _ <- EitherT.pure[F, Throwable](logger.info("Finished collecting list of contributors"))
      } yield contributors).value.flatMap{
        case Left(err) =>
          logger.error(s"Failed to collect list of contributors, reason - $err")
          BadRequest(err.getMessage)
        case Right(value) => Ok(value.asJson)
      }
  }.orNotFound

}
