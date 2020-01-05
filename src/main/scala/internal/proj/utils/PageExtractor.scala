package internal.proj.utils

import org.http4s.headers.Link
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Response, Uri}
import org.http4s.syntax.string._


object PageExtractor {

  val HEADER_LINK: CaseInsensitiveString = "Link".ci

  val META_NEXT = "next"

  def next[F[_]](response: Response[F]): Option[Uri] = {
    response.headers.get(HEADER_LINK).flatMap {
      case Link(l) => l.values.collect { case value if value.rel.contains(META_NEXT) => value.uri }.headOption
    }

  }

}
