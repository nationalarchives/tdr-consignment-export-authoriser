package uk.gov.nationalarchives.consignmentexport.authoriser

import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset
import java.util.UUID
import cats.effect.{Blocker, ContextShift, IO, Resource}
import cats.implicits._
import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.ConfigFactory
import graphql.codegen.GetConsignment.getConsignment.{Data, Variables, document}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import uk.gov.nationalarchives.tdr.GraphQLClient
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}
import uk.gov.nationalarchives.aws.utils.Clients.kms
import uk.gov.nationalarchives.aws.utils.KMSUtils
import uk.gov.nationalarchives.consignmentexport.authoriser.Lambda._

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
    input <- IO.fromEither(decode[Input](Source.fromInputStream(input).mkString))
    config <- Blocker[IO].use(ConfigSource.default.loadF[IO, Configuration])
    kmsUtils = KMSUtils(kms, Map("LambdaFunctionName" -> config.function.name))
    graphQLClient = new GraphQLClient[Data, Variables](kmsUtils.decryptValue(config.api.url))
    consignmentId = UUID.fromString(input.methodArn.split("/").last)
    result <- IO.fromFuture(IO(graphQLClient.getResult(new BearerAccessToken(input.authorizationToken), document, Variables(consignmentId).some)))
  } yield {
    val effect = result.errors.headOption match {
      case Some(_) => "Deny"
      case None => "Allow"
    }
    Output(PolicyDocument("2012-10-17", List(Statement(Effect = effect, Resource = input.methodArn)))).asJson.noSpaces
  }

  def process(inputStream: InputStream, outputStream: OutputStream): Unit = {
    for {
      output <- getOutput(inputStream)
      _ <- writeOutput(outputStream, output)
    } yield ()
  }.unsafeRunSync()
}

object Lambda {
  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(executionContext)
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  case class Api(url: String)
  case class LambdaFunction(name: String)
  case class Configuration(api: Api, function: LambdaFunction)
  case class Input(`type`: String, methodArn: String, authorizationToken: String)
  case class Statement(Action: String = "execute-api:Invoke", Effect: String, Resource: String)
  case class PolicyDocument(Version: String, Statement: List[Statement])
  case class Output(policyDocument: PolicyDocument)
}
