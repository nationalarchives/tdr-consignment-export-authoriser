package uk.gov.nationalarchives.consignmentexport.authoriser

import java.io.ByteArrayOutputStream

import io.circe.parser.decode
import io.circe.generic.auto._
import org.mockito.ArgumentCaptor
import uk.gov.nationalarchives.consignmentexport.authoriser.Lambda.Output

class LambdaSpec extends LambdaSpecUtils {
  forAll(inputs) {
    (filename, expectedEffect) => {
      "The process method" should s"$expectedEffect access for file $filename" in {
        stubKmsResponse
        graphqlGetConsignment(filename)

        val output = mock[ByteArrayOutputStream]
        val byteArrayCaptor: ArgumentCaptor[Array[Byte]] = ArgumentCaptor.forClass(classOf[Array[Byte]])
        doNothing.when(output).write(byteArrayCaptor.capture())
        doNothing.when(output).close()
        val input = """{"type": "TOKEN", "methodArn": "a/method/3e133bf3-7a3f-4c56-8e17-f667dc182f02", "authorizationToken": "token"}""".stripMargin
        val stream = new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8.name))
        new Lambda().process(stream, output)
        val res = byteArrayCaptor.getValue.map(_.toChar).mkString
        val document: Output = decode[Output](res).right.value
        document.policyDocument.Statement.head.Effect should equal(expectedEffect)
      }
    }
  }

  "The process method" should "return Deny if the API returns an error" in {
    stubKmsResponse
    graphqlReturnError
    val output = mock[ByteArrayOutputStream]
    val byteArrayCaptor: ArgumentCaptor[Array[Byte]] = ArgumentCaptor.forClass(classOf[Array[Byte]])
    doNothing.when(output).write(byteArrayCaptor.capture())
    doNothing.when(output).close()
    val input = """{"type": "TOKEN", "methodArn": "a/method/3e133bf3-7a3f-4c56-8e17-f667dc182f02", "authorizationToken": "token"}""".stripMargin
    val stream = new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8.name))
    new Lambda().process(stream, output)
    val res = byteArrayCaptor.getValue.map(_.toChar).mkString
    val document: Output = decode[Output](res).right.value
    document.policyDocument.Statement.head.Effect should equal("Deny")
  }
}
