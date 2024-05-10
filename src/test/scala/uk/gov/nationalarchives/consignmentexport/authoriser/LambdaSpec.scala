package uk.gov.nationalarchives.consignmentexport.authoriser

import java.io.ByteArrayOutputStream
import io.circe.parser.decode
import io.circe.generic.auto._
import org.mockito.ArgumentCaptor
import uk.gov.nationalarchives.consignmentexport.authoriser.Lambda.Output
import uk.gov.nationalarchives.tdr.error.HttpException

class LambdaSpec extends LambdaSpecUtils {
  val methodArnRoot = "arn:aws:execute-api:region:account-id:api-id/stage/HTTP-method/"
  val consignmentId = "3e133bf3-7a3f-4c56-8e17-f667dc182f02"
  val backendChecksResourcePath = s"backend-checks/$consignmentId"
  val draftMetadataChecksResourcePath = s"draft-metadata/validate/$consignmentId/fileName.csv"
  
  forAll(inputs) {
    (filename, expectedEffect) => {
      "The process method" should s"$expectedEffect access for file $filename" in {
        stubKmsResponse
        graphqlGetConsignment(filename)
        val output = mock[ByteArrayOutputStream]
        val byteArrayCaptor: ArgumentCaptor[Array[Byte]] = ArgumentCaptor.forClass(classOf[Array[Byte]])
        doNothing.when(output).write(byteArrayCaptor.capture())
        doNothing.when(output).close()
        val input = s"""{"type": "TOKEN", "methodArn": "$methodArnRoot$backendChecksResourcePath", "authorizationToken": "token"}""".stripMargin
        val stream = new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8.name))
        new Lambda().process(stream, output)
        val res = byteArrayCaptor.getValue.map(_.toChar).mkString
        val document: Output = decode[Output](res).right.value
        document.policyDocument.Statement.head.Effect should equal(expectedEffect)
      }
    }
  }

  "The process method" should "return Deny if the API returns a forbidden error" in {
    stubKmsResponse
    graphqlReturnForbiddenError
    val output = mock[ByteArrayOutputStream]
    val byteArrayCaptor: ArgumentCaptor[Array[Byte]] = ArgumentCaptor.forClass(classOf[Array[Byte]])
    doNothing.when(output).write(byteArrayCaptor.capture())
    doNothing.when(output).close()
    val input = s"""{"type": "TOKEN", "methodArn": "$methodArnRoot$backendChecksResourcePath", "authorizationToken": "token"}""".stripMargin
    val stream = new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8.name))
    new Lambda().process(stream, output)
    val res = byteArrayCaptor.getValue.map(_.toChar).mkString
    val document: Output = decode[Output](res).right.value
    document.policyDocument.Statement.head.Effect should equal("Deny")
  }

  "The process method" should "return an error if the API returns a server error" in {
    stubKmsResponse
    graphqlReturnServerError
    val input = s"""{"type": "TOKEN", "methodArn": "$methodArnRoot$backendChecksResourcePath", "authorizationToken": "token"}""".stripMargin
    val stream = new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8.name))
    val exception = intercept[HttpException] {
      new Lambda().process(stream, new ByteArrayOutputStream())
    }
    exception.response.code.code should equal(500)
  }

  "The process method" should "return Deny if the API response is OK but contains a general error" in {
    stubKmsResponse
    graphqlGetConsignment("general_error")
    val input = s"""{"type": "TOKEN", "methodArn": "$methodArnRoot$backendChecksResourcePath", "authorizationToken": "token"}""".stripMargin
    val stream = new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8.name))
    val exception = intercept[RuntimeException] {
      new Lambda().process(stream, new ByteArrayOutputStream())
    }
    exception.getMessage should equal("GraphQL response contained errors: User '4ab14990-ed63-4615-8336-56fbb9960300' does not own consignment '6e3b76c4-1745-4467-8ac5-b4dd736e1b3e'")
  }
}
