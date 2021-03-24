package uk.gov.nationalarchives.consignmentexport.authoriser

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{okJson, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, EitherValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}

import scala.io.Source.fromResource

class LambdaSpecUtils extends AnyFlatSpec with BeforeAndAfterEach with BeforeAndAfterAll with TableDrivenPropertyChecks with Matchers with MockitoSugar with EitherValues {

  val wiremockGraphqlServer = new WireMockServer(9001)
  val wiremockKmsServer = new WireMockServer(9002)

  val graphQlPath = "/graphql"

  def graphQlUrl: String = wiremockGraphqlServer.url(graphQlPath)

  def graphqlGetConsignment(filename: String): StubMapping = wiremockGraphqlServer.stubFor(post(urlEqualTo(graphQlPath))
    .willReturn(okJson(fromResource(s"json/$filename.json").mkString)))

  def stubKmsResponse = wiremockKmsServer.stubFor(post(urlEqualTo("/"))
    .willReturn(okJson(s"""{"Plaintext": "aHR0cDovL2xvY2FsaG9zdDo5MDAxL2dyYXBocWw="}""")))

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
    ("general_error", "Deny"),
    ("no_error", "Allow")
  )
}
