package org.przydrinic.config

import cats.effect.IO
import cats.implicits.catsSyntaxTuple2Parallel
import ciris._

case class AppConfig(apiHost: String, port: Int)

object AppConfig {
  def load: IO[AppConfig] = {
    (
      env("API_HOST").as[String].default("https://api.external.com"),
      env("PORT").as[Int].default(8080)
    ).parMapN(AppConfig.apply).load[IO]
  }
}