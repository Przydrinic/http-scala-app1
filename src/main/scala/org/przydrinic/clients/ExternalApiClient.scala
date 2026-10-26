package org.przydrinic.clients

import cats.effect.Concurrent
import org.http4s.client.Client
import org.http4s.circe.CirceEntityCodec._
import org.http4s.Method.GET
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Uri
import org.przydrinic.domain.MyDomainModel

trait ExternalApiClient[F[_]] {
  def fetchExternalData: F[MyDomainModel]
}

object ExternalApiClient {
  // Pass the raw client and config in at the time of construction
  def make[F[_]: Concurrent](client: Client[F], baseUri: Uri): ExternalApiClient[F] =
    new ExternalApiClient[F] with Http4sClientDsl[F] {

      override def fetchExternalData: F[MyDomainModel] = {
        val request = GET(baseUri / "data")
        // expect[] executes the request, decodes the body, and raises an error on non-2xx status
        client.expect[MyDomainModel](request)
      }
    }
}