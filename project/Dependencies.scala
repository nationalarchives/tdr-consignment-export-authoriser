import sbt._

object Dependencies {
  lazy val awsUtils =  "uk.gov.nationalarchives.aws.utils" %% "tdr-aws-utils" % "0.1.15"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.2.0"
  lazy val generatedGraphql = "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.187"
  lazy val graphqlClient = "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.15"
  lazy val log4cats = "io.chrisdavenport" %% "log4cats-core"    % "1.5.1"
  lazy val log4catsSlf4j = "io.chrisdavenport" %% "log4cats-slf4j"   % "1.5.1"
  lazy val mockitoScala = "org.mockito" %% "mockito-scala" % "1.16.0"
  lazy val mockitoScalaTest = "org.mockito" %% "mockito-scala-scalatest" % "1.16.0"
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.14.1"
  lazy val pureConfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.14.1"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.2"
  lazy val slf4j = "org.slf4j" % "slf4j-simple" % "1.7.30"
}
