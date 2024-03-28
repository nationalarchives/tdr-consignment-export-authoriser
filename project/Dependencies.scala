import sbt._

object Dependencies {
  lazy val generatedGraphql = "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.371"
  lazy val kmsUtils =  "uk.gov.nationalarchives" %% "kms-utils" % "0.1.152"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.5.4"
  lazy val graphqlClient = "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.153"
  lazy val log4cats = "org.typelevel" %% "log4cats-core"    % "2.6.0"
  lazy val log4catsSlf4j = "org.typelevel" %% "log4cats-slf4j"   % "2.6.0"
  lazy val mockitoScala = "org.mockito" %% "mockito-scala" % "1.17.30"
  lazy val mockitoScalaTest = "org.mockito" %% "mockito-scala-scalatest" % "1.17.30"
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.6"
  lazy val pureConfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.6"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.18"
  lazy val slf4j = "org.slf4j" % "slf4j-simple" % "2.0.12"
}
