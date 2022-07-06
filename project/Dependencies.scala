import sbt._

object Dependencies {
  lazy val generatedGraphql = "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.244"
  lazy val awsUtils =  "uk.gov.nationalarchives" %% "tdr-aws-utils" % "0.1.33"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.13"
  lazy val graphqlClient = "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.40"
  lazy val log4cats = "org.typelevel" %% "log4cats-core"    % "2.3.2"
  lazy val log4catsSlf4j = "org.typelevel" %% "log4cats-slf4j"   % "2.3.2"
  lazy val mockitoScala = "org.mockito" %% "mockito-scala" % "1.17.7"
  lazy val mockitoScalaTest = "org.mockito" %% "mockito-scala-scalatest" % "1.17.7"
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.1"
  lazy val pureConfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.1"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.12"
  lazy val slf4j = "org.slf4j" % "slf4j-simple" % "1.7.36"
}
