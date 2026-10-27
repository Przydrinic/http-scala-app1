package org.przydrinic.services

import cats.effect.Async
import cats.effect.Sync
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class AppService[F[_]: Async] {
  implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

  def processRequest(): F[Unit] = {
    logger.info("Processing business logic inside AppService...") *>
      Async[F].unit
  }
}