import sbt._

object Dependencies {
  lazy val kmsUtils =  "uk.gov.nationalarchives" %% "kms-utils" % "0.1.313"
  lazy val generatedGraphql = "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.444"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.6.3"
  lazy val graphqlClient = "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.256"
  lazy val log4cats = "org.typelevel" %% "log4cats-core"    % "2.7.1"
  lazy val log4catsSlf4j = "org.typelevel" %% "log4cats-slf4j"   % "2.7.1"
  lazy val mockitoScala = "org.mockito" %% "mockito-scala" % "2.0.0"
  lazy val mockitoScalaTest = "org.mockito" %% "mockito-scala-scalatest" % "2.0.0"
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.9"
  lazy val pureConfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.9"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.19"
  lazy val slf4j = "org.slf4j" % "slf4j-simple" % "2.0.17"
}
