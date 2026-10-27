package org.przydrinic.http

import cats.effect.Async
import cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import org.przydrinic.clients.GeolocationApiClient
import org.przydrinic.services.AppService
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class ApiRoutes[F[_]: Async](
                              apiClient: GeolocationApiClient[F],
                              appService: AppService[F]
                            ) extends Http4sDsl[F] {

  implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "info"       => handleGeolocation(None)
    case GET -> Root / "api" / "info" / ip  => handleGeolocation(ip.some)
  }

  private def handleGeolocation(ipOpt: Option[String]): F[org.http4s.Response[F]] = {
    val ipDesc = ipOpt.getOrElse("caller IP")

    (for {
      _    <- logger.info(s"Received request for GET /api/info with ip: $ipDesc")
      _    <- appService.processRequest()
      data <- apiClient.fetchExternalData(ipOpt)
      _    <- logger.info("Successfully fetched and decoded external data")
      res  <- Ok(data)
    } yield res).handleErrorWith { error =>
      logger.error(error)(s"Failed to process geolocation request for $ipDesc") *>
        InternalServerError(Map[String, String]("error" -> error.getMessage))
    }
  }
}