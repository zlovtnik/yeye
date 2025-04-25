name := "yeye-backend"
scalaVersion := "3.3.5"

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
  "org.http4s" %% "http4s-ember-server" % "0.23.26",
  "org.http4s" %% "http4s-circe" % "0.23.26",
  "org.http4s" %% "http4s-dsl" % "0.23.26",
  "io.circe" %% "circe-generic" % "0.14.6",
  "com.zaxxer" % "HikariCP" % "5.1.0",
  "org.postgresql" % "postgresql" % "42.7.3",
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.flywaydb" % "flyway-core" % "10.13.0",
  "org.flywaydb" % "flyway-database-oracle" % "10.13.0",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.10.0",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.10.0",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.10.0",
  "org.http4s" %% "http4s-ember-client" % "0.23.27",
  "io.circe" %% "circe-parser" % "0.14.7",
  "com.oracle.database.jdbc" % "ojdbc8" % "23.4.0.24.05",
  "ch.qos.logback" % "logback-classic" % "1.5.6",
  "org.scalameta" %% "munit" % "0.7.29" % Test,
  "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test,
  "com.typesafe" % "config" % "1.4.3",
  "org.typelevel" %% "cats-mtl" % "1.3.1",
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-h2" % "1.0.0-RC4" % Test,
  "com.h2database" % "h2" % "2.2.224" % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.17.0" % Test,
  "org.mockito" % "mockito-core" % "5.11.0" % Test
)

Compile / mainClass := Some("com.yeye.backend.Server")
run / fork := true
run / javaOptions ++= Seq(
  "-Dlogback.configurationFile=logback.xml",
  "-Xmx1G"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF")  => MergeStrategy.discard
  case PathList("META-INF", "services", _*) => MergeStrategy.concat
  case PathList("META-INF", _*)             => MergeStrategy.discard
  case _                                    => MergeStrategy.first
}

assembly / assemblyJarName := "backend.jar"
