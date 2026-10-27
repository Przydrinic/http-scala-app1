package org.przydrinic

import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all._
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import org.http4s.Uri
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.przydrinic.clients.ExternalApiClient
import org.przydrinic.config.AppConfig
import org.przydrinic.http.ApiRoutes
import org.przydrinic.services.AppService

// --- NEW: Imports for Logging and Metrics ---
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.hotspot.DefaultExports
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.server.middleware.Metrics

object Main extends IOApp.Simple {


  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val appResource: Resource[IO, Server] = for {
    config <- Resource.eval(AppConfig.load)

    baseUri <- Resource.eval(
      IO.fromEither(Uri.fromString(config.apiHost))
    )


    registry <- Resource.eval(IO(new CollectorRegistry()))
    _        <- Resource.eval(IO(DefaultExports.initialize())) // CPU, Memory, Threads
    metrics  <- Prometheus.metricsOps[IO](registry, "http4s_server")

    httpClient <- EmberClientBuilder.default[IO].build


    appService     = new AppService[IO]
    externalClient = ExternalApiClient.make[IO](httpClient, baseUri)
    apiRoutes      = new ApiRoutes[IO](externalClient, appService).routes


    metricsRoute = HttpRoutes.of[IO] {
      case GET -> Root / "metrics" =>
        IO(registry.metricFamilySamples()).flatMap { samples =>
          import java.io.StringWriter
          val writer = new StringWriter()
          io.prometheus.client.exporter.common.TextFormat.write004(writer, samples)
          Ok(writer.toString)
        }
    }


    httpApp = (Metrics[IO](metrics)(apiRoutes) <+> metricsRoute).orNotFound

    serverPort = Port.fromInt(config.port).orElse(Port.fromInt(8080)).get
    serverHost = host"0.0.0.0"

    server <- EmberServerBuilder
      .default[IO]
      .withHost(serverHost)
      .withPort(serverPort)
      .withHttpApp(httpApp)
      .build
  } yield server

  override val run: IO[Unit] =
    appResource.use { server =>
      logger.info(s"🚀 Server running on http://${server.address.getHostString}:${server.address.getPort}") *>
        logger.info(s"📊 Metrics available at http://${server.address.getHostString}:${server.address.getPort}/metrics") *>
        IO.never
    }
}