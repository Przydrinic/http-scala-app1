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


  val appResource: Resource[IO, Server] = for {

    config <- Resource.eval(AppConfig.load)


    baseUri <- Resource.eval(
      IO.fromEither(Uri.fromString(config.apiHost))
    )


    httpClient <- EmberClientBuilder.default[IO].build


    externalClient = ExternalApiClient.make[IO](httpClient, baseUri)
    httpApp        = new ApiRoutes[IO](externalClient).routes.orNotFound


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
      IO.println(s"🚀 Server running on http://${server.address.getHostString}:${server.address.getPort}") *>
        IO.never
    }
}

