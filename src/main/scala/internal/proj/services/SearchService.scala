package internal.proj.services

import internal.proj.errors.DomainError
import internal.proj.models.{Contributor, Repo}


trait SearchService[F[_]] {

  def repos(orgName: String): F[Either[DomainError, List[Repo]]]

  def contributors(repo: Repo): F[Either[DomainError, List[Contributor]]]

  def allContributors(repos: List[Repo]): F[Either[DomainError, List[Contributor]]]

}
