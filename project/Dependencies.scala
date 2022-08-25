import sbt._

object Dependencies {
  lazy val generatedGraphql = "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.257"
  lazy val awsUtils =  "uk.gov.nationalarchives" %% "tdr-aws-utils" % "0.1.35"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.14"
  lazy val graphqlClient = "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.49"
  lazy val log4cats = "org.typelevel" %% "log4cats-core"    % "2.4.0"
  lazy val log4catsSlf4j = "org.typelevel" %% "log4cats-slf4j"   % "2.4.0"
  lazy val mockitoScala = "org.mockito" %% "mockito-scala" % "1.17.12"
  lazy val mockitoScalaTest = "org.mockito" %% "mockito-scala-scalatest" % "1.17.12"
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.1"
  lazy val pureConfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.1"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.13"
  lazy val slf4j = "org.slf4j" % "slf4j-simple" % "2.0.0"
}
