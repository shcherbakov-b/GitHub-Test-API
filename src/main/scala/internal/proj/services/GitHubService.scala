package internal.proj.services

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.applicative._
import cats.syntax.either._
import internal.proj.codecs._
import internal.proj.errors.{DecodeFailed, DomainError}
import internal.proj.models.{Contributor, Repo}
import internal.proj.routes.GitHubRoutes
import internal.proj.utils.PageExtractor
import org.http4s._
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}

class GitHubService[F[_] : Sync](
  httpClient: Client[F],
  token: Option[String]) extends SearchService[F] {

  def repos(orgName: String): F[Either[DomainError, List[Repo]]] =
    httpClient.fetch(createRequest(Uri.unsafeFromString(GitHubRoutes.reposRoute(orgName))))(nextPage)

  def contributors(repo: Repo): F[Either[DomainError, List[Contributor]]] =
    httpClient.fetch(createRequest(Uri.unsafeFromString(repo.contributorsUrl))) { response =>
      contributorsDecoder.decode(response, strict = false)
        .leftMap[DomainError](error => DecodeFailed(error.getMessage())).value
    }

  def allContributors(repos: List[Repo]): F[Either[DomainError, List[Contributor]]] =
    repos.foldLeft(EitherT(List.empty[Contributor].asRight[DomainError].pure[F])) {
      (acc, repo) =>
        for {
          contributors <- EitherT(contributors(repo))
          remaining <- acc
        } yield remaining ++ contributors
    }.map(_.groupBy(_.login).map(_._2.reduce(_ merge _))).map(_.toList.sorted).value

  private def nextPage(response: Response[F]): F[Either[DomainError, List[Repo]]] = {
    val page = PageExtractor.next(response)
    val result = reposDecoder.decode(response, strict = false)
      .leftMap[DomainError](error => DecodeFailed(error.getMessage))
    page match {
      case None => result.value
      case Some(uri) =>
        (for {
          next <- EitherT(httpClient.fetch(createRequest(uri))(nextPage))
          res <- result
        } yield next ++ res).value
    }
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
}
