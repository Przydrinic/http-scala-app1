package org.przydrinic.http

import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import org.przydrinic.clients.ExternalApiClient


class ApiRoutes[F[_]: Concurrent](apiClient: ExternalApiClient[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "info" =>
      apiClient.fetchExternalData
        .flatMap(data => Ok(data))
        .handleErrorWith { error =>
          InternalServerError(Map("error" -> error.getMessage))
        }
  }
}