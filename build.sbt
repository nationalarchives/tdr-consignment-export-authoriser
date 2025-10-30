import Dependencies._

ThisBuild / scalaVersion     := "2.13.17"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "tdr-consignment-export-authoriser",
    resolvers ++= Seq[Resolver](
      "TDR Releases" at "s3://tdr-releases-mgmt"
    ),
    libraryDependencies ++= Seq(
      kmsUtils,
      catsEffect,
      generatedGraphql,
      graphqlClient,
      log4cats,
      log4catsSlf4j,
      mockitoScala % Test,
      mockitoScalaTest % Test,
      pureConfig,
      pureConfigCatsEffect,
      scalaTest % Test,
      slf4j
    ),
    (Test / fork) := true,
    (Test / javaOptions) += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.conf",
    (Test / envVars) := Map("AWS_ACCESS_KEY_ID" -> "test", "AWS_SECRET_ACCESS_KEY" -> "test"),
    (assembly / assemblyJarName) := "consignment-export-authoriser.jar",
    (assembly / assemblyMergeStrategy) := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }

  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
