package internal.proj.utils

import org.http4s.headers.Link
import org.http4s.syntax.string._
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Headers, Uri}

object GitHubPageExtractor {

  val HeaderLink: CaseInsensitiveString = "Link".ci

  val MetaLast = "last"

  /**
   * As GitHub implements Web Linking
   * so we can use such function to fetch all possible pages
   * @param headers - headers from response
   * @return list of constructed uri
   */
  def next(headers: Headers): List[Uri] = {
    headers.get(HeaderLink).toList.flatMap {
      case Link(l) =>
        val uri = l.values.collect { case value if value.rel.contains(MetaLast) => value.uri }.headOption
        uri.toList.flatMap { uri =>
          (for (i <- 2 to extractGitHubPage(uri)) yield buildUri(uri, i)).toList
        }
    }

  }

  /**
   * get last page from query params
   * Note: it is safe here cause if we get Link we always get a page
   * @param gitHubUri - uri from Link
   * @return last page
   */
  private def extractGitHubPage(gitHubUri: Uri) = {
    gitHubUri.query.params("page").toInt
  }

  private def buildUri(base: Uri, pageNumber: Int): Uri = {
    Uri.unsafeFromString(base.toString.split("=").head + "=" + pageNumber)
  }

}
