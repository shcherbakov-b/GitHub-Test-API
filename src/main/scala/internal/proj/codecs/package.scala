package internal.proj

import internal.proj.models.{Contributor, Repo}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

package object codecs {

  implicit lazy val repoEncoder: Encoder[Repo] = deriveEncoder[Repo]
  implicit lazy val repoDecoder: Decoder[Repo] = deriveDecoder[Repo]

  implicit lazy val contributorEncoder: Encoder[Contributor] = deriveEncoder[Contributor]
  implicit lazy val contributorDecoder: Decoder[Contributor] = deriveDecoder[Contributor]
}
