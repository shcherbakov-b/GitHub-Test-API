package internal.proj.routes

import cats.data.Kleisli
import cats.effect.Async
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import internal.proj.codecs._
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
        _ <- logger.info(s"Received request to collect contributors of organization - $name").pure[F]
        repos <- service.repos(name)
        _ <- logger.info("Total repos amount - " + repos.size).pure[F]
        contributors <- service.allContributors(repos)
        _ <- logger.info("Finished collecting list of contributors").pure[F]
      } yield contributors).flatMap(value => Ok(value.asJson))
  }.orNotFound

}
