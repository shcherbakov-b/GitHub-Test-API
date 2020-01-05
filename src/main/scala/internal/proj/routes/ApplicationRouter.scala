package internal.proj.routes

import cats.data.{EitherT, Kleisli}
import cats.effect.Async
import cats.syntax.flatMap._
import internal.proj.codecs._
import internal.proj.services.SearchService
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.syntax.kleisli._
import org.http4s.{HttpRoutes, Request, Response}

class ApplicationRouter[F[_] : Async](service: SearchService[F]) extends Http4sDsl[F] {

  val routes: Kleisli[F, Request[F], Response[F]] = HttpRoutes.of[F] {
    case GET -> Root / "org" / name / "contributors" =>
      (for {
        repos <- EitherT(service.repos(name))
        contributors <- EitherT(service.allContributors(repos))
      } yield contributors).value.flatMap {
        case Left(error) => InternalServerError(error.getMessage)
        case Right(value) => Ok(value.asJson)
      }
  }.orNotFound

}
