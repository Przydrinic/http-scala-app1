package org.przydrinic.http

import weaver._
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.syntax.all._
import org.http4s.circe.CirceEntityCodec._
import org.przydrinic.clients.GeolocationApiClient
import org.przydrinic.domain.Location
import org.przydrinic.services.AppService

object ApiRoutesSuite extends SimpleIOSuite {

  val mockSuccessClient: GeolocationApiClient[IO] = new GeolocationApiClient[IO] {
    override def fetchExternalData(ip: Option[String] = None): IO[Location] =
      IO.pure(Location(ip.getOrElse("127.0.0.1"), "US"))
  }

  val mockFailureClient: GeolocationApiClient[IO] = new GeolocationApiClient[IO] {
    override def fetchExternalData(ip: Option[String] = None): IO[Location] =
      IO.raiseError(new RuntimeException("Upstream service down"))
  }

  val mockAppService = new AppService[IO]

  test("GET /api/info returns 200 OK with decoded payload for caller IP") {
    val routes  = new ApiRoutes[IO](mockSuccessClient, mockAppService).routes
    val request = Request[IO](Method.GET, uri"/api/info")

    for {
      response <- routes.orNotFound.run(request)
      body     <- response.as[Location]
    } yield {
      expect.eql(Status.Ok, response.status) &&
        expect(body == Location("127.0.0.1", "US"))
    }
  }

  test("GET /api/info/{ip} returns 200 OK with decoded payload for specific IP") {
    val routes  = new ApiRoutes[IO](mockSuccessClient, mockAppService).routes
    val request = Request[IO](Method.GET, uri"/api/info/8.8.8.8")

    for {
      response <- routes.orNotFound.run(request)
      body     <- response.as[Location]
    } yield {
      expect.eql(Status.Ok, response.status) &&
        expect(body == Location("8.8.8.8", "US"))
    }
  }

  test("GET /api/info handles upstream failures with 500 InternalServerError") {
    val routes  = new ApiRoutes[IO](mockFailureClient, mockAppService).routes
    val request = Request[IO](Method.GET, uri"/api/info")

    for {
      response <- routes.orNotFound.run(request)
    } yield {
      expect.eql(Status.InternalServerError, response.status)
    }
  }

  test("GET /invalid returns 404 Not Found") {
    val routes  = new ApiRoutes[IO](mockSuccessClient, mockAppService).routes
    val request = Request[IO](Method.GET, uri"/invalid")

    for {
      response <- routes.orNotFound.run(request)
    } yield {
      expect.eql(Status.NotFound, response.status)
    }
  }
}