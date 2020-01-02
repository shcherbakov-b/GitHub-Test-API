package internal.proj.routes

object GitHubRoutes {

  val GitHubApi: String = "https://api.github.com"

  def reposRoute(orgName: String): String = GitHubApi + s"/orgs/$orgName/repos"

}
