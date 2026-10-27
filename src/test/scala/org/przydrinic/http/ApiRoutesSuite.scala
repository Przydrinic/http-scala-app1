package org.przydrinic.http
// (Imports remain the same)
import weaver._
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.syntax.all._
import org.http4s.circe.CirceEntityCodec._
import org.przydrinic.clients.ExternalApiClient
import org.przydrinic.domain.MyDomainModel
import org.przydrinic.services.AppService // NEW

object ApiRoutesSuite extends SimpleIOSuite {

  val mockSuccessClient: ExternalApiClient[IO] = new ExternalApiClient[IO] {
    override def fetchExternalData: IO[MyDomainModel] = IO.pure(MyDomainModel("test_payload"))
  }

  val mockFailureClient: ExternalApiClient[IO] = new ExternalApiClient[IO] {
    override def fetchExternalData: IO[MyDomainModel] = IO.raiseError(new RuntimeException("Upstream service down"))
  }

  // --- NEW: Instantiate a dummy app service for the tests ---
  val mockAppService = new AppService[IO]

  test("GET /api/info returns 200 OK with decoded payload") {
    val routes  = new ApiRoutes[IO](mockSuccessClient, mockAppService).routes // Pass it here
    val request = Request[IO](Method.GET, uri"/api/info")

    for {
      response <- routes.orNotFound.run(request)
      body     <- response.as[MyDomainModel]
    } yield {
      expect.eql(Status.Ok, response.status) &&
        expect(body == MyDomainModel("test_payload"))
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