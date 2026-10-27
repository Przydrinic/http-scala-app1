package org.przydrinic.clients

import weaver._
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.syntax.all._
import org.przydrinic.domain.Location

object GeolocationApiClientSuite extends SimpleIOSuite {

  test("GeolocationApiClient fetches caller IP data successfully when no IP is provided") {
    val mockApp = HttpApp[IO] {
      case Method.GET -> Root =>
        Ok(Location("192.168.1.1", "US"))
      case _ =>
        NotFound()
    }

    val httpClient = Client.fromHttpApp(mockApp)
    val client = GeolocationApiClient.make[IO](httpClient, uri"https://country.is")

    for {
      result <- client.fetchExternalData(None)
    } yield {
      expect(Location("192.168.1.1", "US") == result)
    }
  }

  test("GeolocationApiClient fetches specific IP data successfully when IP is provided") {
    val mockApp = HttpApp[IO] {
      case Method.GET -> Root / "8.8.8.8" =>
        Ok(Location("8.8.8.8", "US"))
      case _ =>
        NotFound()
    }

    val httpClient = Client.fromHttpApp(mockApp)
    val client = GeolocationApiClient.make[IO](httpClient, uri"https://country.is")

    for {
      result <- client.fetchExternalData("8.8.8.8".some)
    } yield {
      expect(Location("8.8.8.8", "US") == result)
    }
  }

  test("GeolocationApiClient handles upstream failure gracefully") {
    val mockApp = HttpApp[IO] {
      case _ =>
        InternalServerError("Upstream error")
    }

    val httpClient = Client.fromHttpApp(mockApp)
    val client = GeolocationApiClient.make[IO](httpClient, uri"https://country.is")

    for {
      attempt <- client.fetchExternalData(None).attempt
    } yield {
      attempt match {
        case Left(error) => expect(error.getMessage.contains("500"))
        case Right(_)    => failure("Expected failure but got success")
      }
    }
  }
}