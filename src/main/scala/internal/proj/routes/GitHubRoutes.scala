package internal.proj.routes

object GitHubRoutes {

  val GitHubApiUrl: String = "https://api.github.com"

  def reposRoute(orgName: String): String = GitHubApiUrl + s"/orgs/$orgName/repos"

}
