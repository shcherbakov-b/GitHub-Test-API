package internal.proj.routes

import cats.data.{EitherT, Kleisli}
import cats.effect.Async
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.either._
import internal.proj.codecs._
import internal.proj.errors.DomainError
import internal.proj.models.{Contributor, Repo}
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
        startTime <- EitherT(System.currentTimeMillis().asRight[DomainError].pure[F])
        _ <- EitherT(logger.warn("Start - "  + startTime / 1000).asRight[DomainError].pure[F])
        repos <- EitherT.liftF[F, DomainError, List[Repo]](service.repos(name))
        _ <- EitherT(logger.warn("Total repos amount = " + repos.size).asRight[DomainError].pure[F])
        contributors <- EitherT.liftF[F, DomainError, List[Contributor]](service.allContributors(repos))
        _ <- EitherT(logger.warn("End - "  + (System.currentTimeMillis() - startTime) / 1000).asRight[DomainError].pure[F])
      } yield contributors).value.flatMap {
        case Left(error) => InternalServerError(error.getMessage)
        case Right(value) => Ok(value.asJson)
      }
  }.orNotFound

}
