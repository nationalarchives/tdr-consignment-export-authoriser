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

  val graphQlPath = "/graphql"

  def graphQlUrl: String = wiremockGraphqlServer.url(graphQlPath)

  def graphqlGetConsignment(filename: String): StubMapping = wiremockGraphqlServer.stubFor(post(urlEqualTo(graphQlPath))
    .willReturn(okJson(fromResource(s"json/$filename.json").mkString)))

  override def beforeEach(): Unit = {
    wiremockGraphqlServer.resetAll()
  }

  override def beforeAll(): Unit = {
    wiremockGraphqlServer.start()
  }

  override def afterAll(): Unit = {
    wiremockGraphqlServer.stop()
  }

  val inputs: TableFor2[String, String] = Table(
    ("filename", "expectedEffect"),
    ("auth_error", "Deny"),
    ("general_error", "Deny"),
    ("no_error", "Allow")
  )
}
