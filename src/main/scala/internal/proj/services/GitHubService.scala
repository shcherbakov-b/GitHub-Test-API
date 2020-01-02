package internal.proj.services
import internal.proj.models.{Contributor, Repo}
import org.http4s.client.blaze.BlazeClientBuilder

class GitHubService(httpClient: BlazeClientBuilder)[F[_]] extends SearchService[F] {


  def repos(orgName: String): F[List[Repo]] = ???
  def contributors(repo: Repo): F[List[Contributor]] = ???
}
