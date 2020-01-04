package internal.proj.services

import cats.Monad
import cats.data.EitherT
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.flatMap._
import internal.proj.models.{Contributor, Repo}
import internal.proj.routes.GitHubRoutes
import internal.proj.utils.PageExtractor
import org.http4s.{DecodeFailure, DecodeResult, EntityDecoder, Request, Response, Uri}
import org.http4s.Status.Successful
import org.http4s.client.Client

class GitHubService[F[_] : Monad](httpClient: Client[F]) extends SearchService[F] {

  type DecodeRepos = F[Either[DecodeFailure, List[Repo]]]

  def repos(orgName: String)(implicit decoder: EntityDecoder[F, List[Repo]]): F[List[Repo]] =
    httpClient.get[List[Repo]](GitHubRoutes.reposRoute(orgName)) { response =>
      nextPage(response).map(_.getOrElse(List.empty))
      //      case Successful(resp) =>
      //        println(PageExtractor.next(resp))
      //        val r = decoder.decode(resp, strict = false)
      //          .leftMap { error =>
      //            print(error)
      //            List.empty[Repo]
      //          }.merge
      //        r.map { repos =>
      //          print(repos)
      //          repos
      //        }
      //
      //      case _ => List.empty[Repo].pure[F]
    }

  def contributors(repo: Repo)(implicit decoder: EntityDecoder[F, List[Contributor]]): F[List[Contributor]] =
    httpClient.fetch(Request[F](uri = Uri.unsafeFromString(repo.contributorsUrl))) { response =>
      decoder.decode(response, strict = false).leftMap { error =>
        println(error)
        List.empty[Contributor]
      }.merge
    }

  def allContributors(repos: List[Repo])(implicit decoder: EntityDecoder[F, List[Contributor]]): F[List[Contributor]] =
    repos.foldLeft(List.empty[Contributor].pure[F]) {
      (acc, el) => contributors(el).flatMap(c => acc.map(_ ++ c))
    }.map(_.groupBy(_.login).map(_._2.reduce(_ merge _))).map(_.toList.sortBy(_.contributions))

  private def nextPage(response: Response[F])(implicit decoder: EntityDecoder[F, List[Repo]]): DecodeRepos = {
    val page = PageExtractor.next(response)
    val result: DecodeResult[F, List[Repo]] = decoder.decode(response, strict = false)
    page match {
      case None => result.value
      case Some(uri) =>
        val res: F[Either[DecodeFailure, List[Repo]]] = httpClient.get(uri) { resp =>
          nextPage(resp)
        }
        (for {
          next <- EitherT(res)
          r <- result
        } yield next ++ r).value
    }
  }
}
