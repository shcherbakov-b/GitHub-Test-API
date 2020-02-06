package internal.proj.utils

import org.http4s.headers.Link
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Response, Uri}
import org.http4s.syntax.string._

object PageExtractor {

  val HeaderLink: CaseInsensitiveString = "Link".ci

  val MetaLast = "last"

  def next[F[_]](response: Response[F]): List[Uri] = {
    response.headers.get(HeaderLink).toList.flatMap {
      case Link(l) =>
        val uri = l.values.collect { case value if value.rel.contains(MetaLast) => value.uri }.headOption
        uri.toList.flatMap { uri =>
          (for (i <- 2 to extractGitHubPage(uri)) yield buildUri(uri, i)).toList
        }
    }

  }

  private def extractGitHubPage(gitHubUri: Uri) = {
    gitHubUri.query.params("page").toInt
  }

  private def buildUri(base: Uri, pageNumber: Int): Uri = {
    Uri.unsafeFromString(base.toString.split("=").head + "=" + pageNumber)
  }

}
