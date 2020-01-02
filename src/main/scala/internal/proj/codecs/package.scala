package internal.proj

import internal.proj.models.{Contributor, Repo}
//import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

package object codecs {

  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit lazy val repoEncoder: Encoder[Repo] = deriveConfiguredEncoder[Repo]
  implicit lazy val repoDecoder: Decoder[Repo] = deriveConfiguredDecoder[Repo]

  implicit lazy val contributorEncoder: Encoder[Contributor] = deriveConfiguredEncoder[Contributor]
  implicit lazy val contributorDecoder: Decoder[Contributor] = deriveConfiguredDecoder[Contributor]
}
