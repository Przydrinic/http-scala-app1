package org.przydrinic.clients

import weaver._
import cats.effect.IO
import org.http4s.client.Client
import org.http4s.HttpApp
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.syntax.all._
import org.przydrinic.clients.ExternalApiClient
import org.przydrinic.domain.MyDomainModel

object ExternalApiClientSuite extends SimpleIOSuite {

  val mockExternalApi = HttpApp[IO] {
    case GET -> Root / "data" => Ok(MyDomainModel("success_value"))
    case _                    => NotFound()
  }

  val mockClient = Client.fromHttpApp(mockExternalApi)

  test("fetchExternalData executes request and decodes JSON correctly") {
    val apiClient = ExternalApiClient.make[IO](mockClient, uri"http://localhost")

    apiClient.fetchExternalData.map { response =>
      expect(response.value == "success_value")
    }
  }
}