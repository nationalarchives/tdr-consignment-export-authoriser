package uk.gov.nationalarchives.consignmentexport.authoriser

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits._
import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import graphql.codegen.GetConsignment.getConsignment.{Data, Variables, document}
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}
import sttp.model.StatusCode
import uk.gov.nationalarchives.aws.utils.kms.KMSClients.kms
import uk.gov.nationalarchives.aws.utils.kms.KMSUtils
import uk.gov.nationalarchives.consignmentexport.authoriser.Lambda._
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.error.{HttpException, NotAuthorisedError}

import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset
import java.util.UUID
import scala.concurrent.ExecutionContextExecutor
import scala.io.Source

class Lambda {

  def writeOutput(outputStream: OutputStream, output: String): IO[Unit] =
    Resource.make {
      IO(outputStream)
    } { outStream =>
      IO(outStream.close()).handleErrorWith(_ => IO.unit)
    }.use {
      o => IO(o.write(output.getBytes(Charset.forName("UTF-8"))))
    }

  def getOutput(input: InputStream) = for {
    logger <- Slf4jLogger.create[IO]
    _ <- logger.info("Decoding input")
    input <- IO.fromEither(decode[Input](Source.fromInputStream(input).mkString))
    config <- ConfigSource.default.loadF[IO, Configuration]
    consignmentId = extractConsignmentId(input.methodArn)

    _ <- logger.info("Loading and decrypting config")
    kmsUtils = KMSUtils(kms(config.kms.endpoint), Map("LambdaFunctionName" -> config.function.name))
    decryptedConfig = kmsUtils.decryptValue(config.api.url)

    _ <- logger.info(s"Calling API to check user's permissions for consignment '$consignmentId'")
    graphQLClient = new GraphQLClient[Data, Variables](decryptedConfig)
    effect <- IO.fromFuture(IO(graphQLClient.getResult(new BearerAccessToken(input.authorizationToken), document, Variables(consignmentId).some))).attempt.map {
      case Left(e: HttpException) if e.response.code == StatusCode.Forbidden => "Deny"
      case Left(e: Throwable) => throw e
      case Right(response) => response.errors match {
        case Nil => "Allow"
        case List(_: NotAuthorisedError) => "Deny"
        case errors => throw new RuntimeException(s"GraphQL response contained errors: ${errors.map(e => e.message).mkString}")
      }
    }
    _ <- logger.info(s"Got result from API")
  } yield Output(PolicyDocument("2012-10-17", List(Statement(Effect = effect, Resource = input.methodArn)))).asJson.noSpaces

  def process(inputStream: InputStream, outputStream: OutputStream): Unit = {
    for {
      output <- getOutput(inputStream)
      _ <- writeOutput(outputStream, output)
    } yield ()
  }.unsafeRunSync()
}

object Lambda {
  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  case class Api(url: String)
  case class LambdaFunction(name: String)
  case class Kms(endpoint: String)
  case class Configuration(api: Api, function: LambdaFunction, kms: Kms)
  case class Input(`type`: String, methodArn: String, authorizationToken: String)
  case class Statement(Action: String = "execute-api:Invoke", Effect: String, Resource: String)
  case class PolicyDocument(Version: String, Statement: List[Statement])
  case class Output(policyDocument: PolicyDocument)

  def extractConsignmentId(methodArn: String): UUID = {
    methodArn.split("/") match {
      case Array(_, _, _, "backend-checks", consignmentId) => UUID.fromString(consignmentId)
      case Array(_, _, _, "export", consignmentId) => UUID.fromString(consignmentId)
      case Array(_, _, _, "draft-metadata", "validate", consignmentId, _) => UUID.fromString(consignmentId)
      case _ => throw new IllegalArgumentException(s"Unexpected path in method arn $methodArn")
    }
  }
}
