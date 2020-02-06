package internal.proj.services

import cats.effect.Async
import cats.instances.list._
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monoid, Parallel}
import internal.proj.codecs._
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

  /**
   * Fetch all repositories of organization
   *
   * @param orgName - organization name
   * @return list of repositories
   */
  def repos(orgName: String): F[List[Repo]] =
    httpClient.fetch(createRequest(Uri.unsafeFromString(GitHubRoutes.reposRoute(orgName))))(nextPage)

  /**
   * Fetch contributors of given repository
   *
   * @param repo - organizations's repository
   * @return contributors of given repository
   */
  def contributors(repo: Repo): F[List[Contributor]] =
    httpClient.fetch(createRequest(Uri.unsafeFromString(repo.contributorsUrl))) {
      case response if response.status == Status.Ok => decode(response)
      case any =>
        logger.warn(s"Failed to get info from repo - ${repo.name}. " +
          s"Received response with status - ${any.status.code}")
        List.empty[Contributor].pure[F]
    }

  /**
   * Collect and sort all contributors of given organization's repositories
   *
   * @param repos - all organization's repositories
   * @return - sorted list of contributors
   */
  def allContributors(repos: List[Repo]): F[List[Contributor]] = {
    Async.parTraverseN(50)(repos)(contributors)
      .map(_.foldLeft(List.empty[Contributor]) {
        (acc, el) => acc ++ el
      }).map(_.groupBy(_.login).map(_._2.reduce(_ merge _))).map(_.toList.sorted)
  }

  /**
   * Traverse through all pages
   *
   * @param response - first response
   * @return decoded repositories
   */
  private def nextPage(response: Response[F]): F[List[Repo]] = {
    val pages = PageExtractor.next(response)
    val result = decode[List[Repo]](response)
    val others = Async.parTraverseN(50)(pages)(uri => httpClient.fetch(createRequest(uri))(decode[List[Repo]]))
    for {
      res <- result
      others <- others
    } yield others.flatten ++ res
  }

  /**
   * Constructing request
   * Adding token and content-type headers
   *
   * @param uri - GitHub uri
   * @return ready to execute request
   */
  private def createRequest(uri: Uri) = {
    val headers = token.fold(Headers.of(Accept(MediaType.application.json)))(token =>
      Headers.of(Authorization(Credentials.Token(AuthScheme.Bearer, token)),
        Accept(MediaType.application.json)))
    Request[F](
      uri = uri,
      headers = headers
    )
  }

  /**
   * Attempt to decode response
   *
   * @param msg     - GitHub response
   * @param decoder - Polymorphic decoder
   * @param monoid  - Monoid instance
   * @return log error and return empty object in case of decoding failure or decoded object
   */
  private def decode[A](msg: Message[F])(implicit decoder: EntityDecoder[F, A], monoid: Monoid[A]): F[A] = {
    decoder.decode(msg, strict = false).leftMap { error =>
      logger.error("Failed to decode: " + error.getMessage())
      monoid.empty
    }.merge
  }
}
