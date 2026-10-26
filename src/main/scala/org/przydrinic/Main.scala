package org.przydrinic

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import org.http4s.Uri
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.przydrinic.clients.ExternalApiClient
import org.przydrinic.config.AppConfig
import org.przydrinic.http.ApiRoutes

object Main extends IOApp.Simple {

  // Assembles all app resources (Client, Service, Server) in order
  val appResource: Resource[IO, Server] = for {
    // 1. Load pure config
    config <- Resource.eval(AppConfig.load)

    // 2. Parse target Uri
    baseUri <- Resource.eval(
      IO.fromEither(Uri.fromString(config.apiHost))
    )

    // 3. Acquire Ember Client (manages connection pooling & shutdown)
    httpClient <- EmberClientBuilder.default[IO].build

    // 4. Instantiate client and routes
    externalClient = ExternalApiClient.make[IO](httpClient, baseUri)
    httpApp        = new ApiRoutes[IO](externalClient).routes.orNotFound

    // 5. Parse IP & Port for network socket binding
    serverPort = Port.fromInt(config.port).orElse(Port.fromInt(8080)).get
    serverHost = host"0.0.0.0"

    // 6. Bind Ember Server
    server <- EmberServerBuilder
      .default[IO]
      .withHost(serverHost)
      .withPort(serverPort)
      .withHttpApp(httpApp)
      .build
  } yield server

  override val run: IO[Unit] =
    appResource.use { server =>
      IO.println(s"🚀 Server running on http://${server.address.getHostString}:${server.address.getPort}") *>
        IO.never // Keep the process alive until SIGINT / SIGTERM
    }
}

