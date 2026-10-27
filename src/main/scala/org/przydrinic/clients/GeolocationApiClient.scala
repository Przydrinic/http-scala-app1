package org.przydrinic.clients

import cats.effect.Async
import cats.syntax.all._
import org.http4s.client.Client
import org.http4s.circe.CirceEntityCodec._
import org.http4s.Method.GET
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Uri
import org.przydrinic.domain.Location
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait GeolocationApiClient[F[_]] {
  def fetchExternalData(ip: Option[String] = None): F[Location]
}

object GeolocationApiClient {

  def make[F[_]: Async](client: Client[F], baseUri: Uri): GeolocationApiClient[F] =
    new GeolocationApiClient[F] with Http4sClientDsl[F] {

      implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

      override def fetchExternalData(ip: Option[String] = None): F[Location] = {
        val targetUri = ip match {
          case Some(address) => baseUri / address
          case None          => baseUri
        }

        val request = GET(targetUri)

        logger.info(s"Fetching external data from $targetUri") *>
          client.expect[Location](request).onError {
            case error => logger.error(error)(s"Failed to fetch external data from $targetUri: ${error.getMessage}")
          } <* logger.info(s"Successfully fetched and decoded external data from $targetUri")
      }
    }
}