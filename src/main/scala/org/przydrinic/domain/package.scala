package org.przydrinic

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object domain {

  final case class MyDomainModel(value: String)

  object MyDomainModel {
    implicit val decoder: Decoder[MyDomainModel] = deriveDecoder[MyDomainModel]
    implicit val encoder: Encoder[MyDomainModel] = deriveEncoder[MyDomainModel]
  }


}
