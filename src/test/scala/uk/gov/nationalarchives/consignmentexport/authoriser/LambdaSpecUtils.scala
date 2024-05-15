package uk.gov.nationalarchives.consignmentexport.authoriser

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.{forbidden, okJson, post, serverError, urlEqualTo}
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, EitherValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}

import java.nio.ByteBuffer
import java.nio.charset.Charset
import scala.io.Source.fromResource
import io.circe.generic.auto._
import io.circe.parser.decode

class LambdaSpecUtils extends AnyFlatSpec with BeforeAndAfterEach with BeforeAndAfterAll with TableDrivenPropertyChecks with Matchers with MockitoSugar with EitherValues {

  val wiremockGraphqlServer = new WireMockServer(9001)
  val wiremockKmsServer = new WireMockServer(new WireMockConfiguration().port(9002).extensions(new ResponseDefinitionTransformer {
    override def transform(request: Request, responseDefinition: ResponseDefinition, files: FileSource, parameters: Parameters): ResponseDefinition = {
      case class KMSRequest(CiphertextBlob: String)
      decode[KMSRequest](request.getBodyAsString) match {
        case Left(err) => throw err
        case Right(req) =>
          val charset = Charset.defaultCharset()
          val plainText = charset.newDecoder.decode(ByteBuffer.wrap(req.CiphertextBlob.getBytes(charset))).toString
          ResponseDefinitionBuilder
            .like(responseDefinition)
            .withBody(s"""{"Plaintext": "$plainText"}""")
            .build()
      }
    }
    override def getName: String = ""
  }))

  val graphQlPath = "/graphql"

  def graphQlUrl: String = wiremockGraphqlServer.url(graphQlPath)

  def graphqlGetConsignment(filename: String): StubMapping = wiremockGraphqlServer.stubFor(post(urlEqualTo(graphQlPath))
    .willReturn(okJson(fromResource(s"json/$filename.json").mkString)))

  def graphqlReturnForbiddenError: StubMapping = wiremockGraphqlServer.stubFor(post(urlEqualTo(graphQlPath))
    .willReturn(forbidden()))

  def graphqlReturnServerError: StubMapping = wiremockGraphqlServer.stubFor(post(urlEqualTo(graphQlPath))
    .willReturn(serverError()))

  def stubKmsResponse = wiremockKmsServer.stubFor(post(urlEqualTo("/")))

  override def beforeEach(): Unit = {
    wiremockGraphqlServer.resetAll()
    wiremockKmsServer.resetAll()
  }

  override def beforeAll(): Unit = {
    wiremockGraphqlServer.start()
    wiremockKmsServer.start()
  }

  override def afterAll(): Unit = {
    wiremockGraphqlServer.stop()
    wiremockKmsServer.stop()
  }

  val inputs: TableFor2[String, String] = Table(
    ("filename", "expectedEffect"),
    ("auth_error", "Deny"),
    ("no_error", "Allow")
  )

  val methodArnRoot = "arn:aws:execute-api:region:account-id:api-id/stage/HTTP-method/"
  val consignmentId = "3e133bf3-7a3f-4c56-8e17-f667dc182f02"
  val exportResourcePath = s"export/$consignmentId"
  val backendChecksResourcePath = s"backend-checks/$consignmentId"
  val draftMetadataChecksResourcePath = s"draft-metadata/validate/$consignmentId/fileName.csv"
  
  val resourcePaths: Seq[String] = Seq(exportResourcePath, backendChecksResourcePath, draftMetadataChecksResourcePath)
}
