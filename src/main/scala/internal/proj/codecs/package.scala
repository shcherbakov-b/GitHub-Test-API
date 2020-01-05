package internal.proj

import cats.effect.Sync
import internal.proj.models.{Contributor, Repo}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

package object codecs {

  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults

  implicit lazy val repoEncoder: Encoder[Repo] = deriveConfiguredEncoder[Repo]
  implicit lazy val repoDecoder: Decoder[Repo] = deriveConfiguredDecoder[Repo]

  implicit lazy val contributorEncoder: Encoder[Contributor] =
    Encoder.forProduct2("name", "contributions")(c => (c.login, c.contributions))
  implicit lazy val contributorDecoder: Decoder[Contributor] = deriveConfiguredDecoder[Contributor]

  implicit def reposDecoder[F[_]: Sync]: EntityDecoder[F, List[Repo]] = jsonOf[F, List[Repo]]
  implicit def contributorsDecoder[F[_]: Sync]: EntityDecoder[F, List[Contributor]] = jsonOf[F, List[Contributor]]
}
