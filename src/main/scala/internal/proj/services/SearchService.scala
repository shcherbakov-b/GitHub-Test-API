package internal.proj.services

import internal.proj.models.{Contributor, Repo}
import org.http4s.EntityDecoder


trait SearchService[F[_]] {

  def repos(orgName: String)(implicit decoder: EntityDecoder[F, List[Repo]]): F[List[Repo]]

  def contributors(repo: Repo): F[List[Contributor]]

}
