package internal.proj.services

import cats.Monad
import cats.syntax.applicative._
import cats.syntax.functor._
import internal.proj.models.{Contributor, Repo}
import internal.proj.routes.GitHubRoutes
import org.http4s.EntityDecoder
import org.http4s.Status.Successful
import org.http4s.client.Client

class GitHubService[F[_] : Monad](httpClient: Client[F]) extends SearchService[F] {

  def repos(orgName: String)(implicit decoder: EntityDecoder[F, List[Repo]]): F[List[Repo]] =
    httpClient.get[List[Repo]](GitHubRoutes.reposRoute(orgName)) {
      case Successful(resp) =>
        val r = decoder.decode(resp, strict = false)
        .leftMap{error =>
          print(error)
          List.empty[Repo]}.merge
          r.map { repos =>
            print(repos)
            repos
          }

    case _ => List.empty[Repo].pure[F]
    }
  def contributors(repo: Repo): F[List[Contributor]] = throw new Error
}
