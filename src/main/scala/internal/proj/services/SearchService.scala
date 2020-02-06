package internal.proj.services

import internal.proj.models.{Contributor, Repo}


trait SearchService[F[_]] {

  def repos(orgName: String): F[List[Repo]]

  def contributors(repo: Repo): F[List[Contributor]]

  def allContributors(repos: List[Repo]): F[List[Contributor]]

}
