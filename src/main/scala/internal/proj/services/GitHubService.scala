package internal.proj.services

import cats.{Monoid, Parallel}
import cats.data.EitherT
import cats.effect.{Async, Sync}
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.instances.list._
import cats.syntax.either._
import internal.proj.codecs._
import internal.proj.errors.{DecodeFailed, DomainError}
import internal.proj.models.{Contributor, Repo}
import internal.proj.routes.GitHubRoutes
import internal.proj.utils.PageExtractor
import org.http4s._
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}
import org.slf4j.LoggerFactory

class GitHubService[F[_] : Async : Parallel](
  httpClient: Client[F],
  token: Option[String]) extends SearchService[F] {

  private val logger = LoggerFactory.getLogger(getClass)

  def repos(orgName: String): F[List[Repo]] =
    httpClient.fetch(createRequest(Uri.unsafeFromString(GitHubRoutes.reposRoute(orgName))))(nextPage)

  def contributors(repo: Repo): F[List[Contributor]] =
    httpClient.fetch(createRequest(Uri.unsafeFromString(repo.contributorsUrl))) {
      case response if response.status == Status.Ok => decode(response)
      case any =>
        logger.warn(s"Failed to get info from repo - ${repo.name}. " +
          s"Received response with status - ${any.status.code}")
        List.empty[Contributor].pure[F]
    }

  //  def allContributors(repos: List[Repo]): F[Either[DomainError, List[Contributor]]] =
  //    repos.foldLeft(EitherT(List.empty[Contributor].asRight[DomainError].pure[F])) {
  //      (acc, repo) =>
  //        for {
  //          contributors <- EitherT(contributors(repo))
  //          remaining <- acc
  //        } yield remaining ++ contributors
  //    }.map(_.groupBy(_.login).map(_._2.reduce(_ merge _))).map(_.toList.sorted).value

  def allContributors(repos: List[Repo]): F[List[Contributor]] = {
    Async.parTraverseN(50)(repos)(contributors)
      .map(_.foldLeft(List.empty[Contributor]) {
        (acc, el) => acc ++ el
      }).map(_.groupBy(_.login).map(_._2.reduce(_ merge _))).map(_.toList.sorted)
  }

  private def nextPage(response: Response[F]): F[List[Repo]] = {
    val pages = PageExtractor.next(response)
    val result = decode[List[Repo]](response)
    val others = Async.parTraverseN(50)(pages)(uri => httpClient.fetch(createRequest(uri))(decode[List[Repo]]))
    for {
      res <- result
      others <- others
    } yield others.flatten ++ res
  }

  private def createRequest(uri: Uri) = {
    val headers = token.fold(Headers.of(Accept(MediaType.application.json)))(t =>
      Headers.of(Authorization(Credentials.Token(AuthScheme.Bearer, t)),
        Accept(MediaType.application.json)))
    Request[F](
      uri = uri,
      headers = headers
    )
  }

  private def decodeRepo(response: Response[F]) = {
    reposDecoder.decode(response, strict = false)
      .leftMap[DomainError](error => DecodeFailed(error.getMessage)).value
  }

  private def decode[A](msg: Message[F])(implicit decoder: EntityDecoder[F, A], monoid: Monoid[A]): F[A] = {
    decoder.decode(msg, strict = false).leftMap { error =>
      logger.error("Failed to decode: " + error.getMessage())
      monoid.empty
    }.merge
  }
}
