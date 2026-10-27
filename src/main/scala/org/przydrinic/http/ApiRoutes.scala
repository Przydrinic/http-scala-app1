package org.przydrinic.http

import cats.effect.Async
import cats.effect.Sync
import cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import org.przydrinic.clients.ExternalApiClient
import org.przydrinic.services.AppService

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class ApiRoutes[F[_]: Async](
                              apiClient: ExternalApiClient[F],
                              appService: AppService[F]
                            ) extends Http4sDsl[F] {

  implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "info" =>
      logger.info("Received request for GET /api/info") *>
        appService.processRequest() *>
        apiClient.fetchExternalData
          .flatMap(data => logger.info("Responding with 200 OK") *> Ok(data))
          .handleErrorWith { error =>
            logger.error(error)("Responding with 500 InternalServerError") *>
              InternalServerError(Map("error" -> error.getMessage))
          }
  }
}