package org.przydrinic.clients

import cats.effect.Async
import cats.effect.Sync
import cats.syntax.all._
import org.http4s.client.Client
import org.http4s.circe.CirceEntityCodec._
import org.http4s.Method.GET
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Uri
import org.przydrinic.domain.MyDomainModel


import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait ExternalApiClient[F[_]] {
  def fetchExternalData: F[MyDomainModel]
}

object ExternalApiClient {

  def make[F[_]: Async](client: Client[F], baseUri: Uri): ExternalApiClient[F] =
    new ExternalApiClient[F] with Http4sClientDsl[F] {

      implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

      override def fetchExternalData: F[MyDomainModel] = {
        val request = GET(baseUri / "data")

        logger.info(s"Fetching external data from ${baseUri}/data") *>
          client.expect[MyDomainModel](request).onError {
            case error => logger.error(error)(s"Failed to fetch external data: ${error.getMessage}")
          } <* logger.info("Successfully fetched and decoded external data")
      }
    }
}