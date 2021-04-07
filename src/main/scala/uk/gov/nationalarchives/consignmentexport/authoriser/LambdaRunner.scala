package uk.gov.nationalarchives.consignmentexport.authoriser

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import cats.effect.IO
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

// An entry point so that you can run the authoriser in a development environment
object LambdaRunner extends App {
  val lambda = new Lambda

  val consignmentId = sys.env("CONSIGNMENT_ID")
  val accessToken = sys.env("ACCESS_TOKEN")

  val message =
    s"""
       |{
       |  "type": "placeholder-type",
       |  "methodArn": "placeholder/${consignmentId}",
       |  "authorizationToken": "${accessToken}"
       |}
       |""".stripMargin
  val inputStream = new ByteArrayInputStream(message.getBytes)

  val outputStream = new ByteArrayOutputStream()

  lambda.process(inputStream, outputStream)

  (for {
    logger <- Slf4jLogger.create[IO]
    _ <- logger.info("Lambda output stream:")
    _ <- logger.info(outputStream.toString)
  } yield "things").unsafeRunSync()

  println("After slf4j")
}
