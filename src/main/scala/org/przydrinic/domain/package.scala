package org.przydrinic

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object domain {


  final case class Location(ip: String, country: String)

  object Location {
    implicit val decoder: Decoder[Location] = deriveDecoder[Location]
    implicit val encoder: Encoder[Location] = deriveEncoder[Location]
  }



}
