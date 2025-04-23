ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.yeye"

val sharedDependencies = Seq(
  "org.typelevel" %% "cats-core" % "2.10.0",
  "io.circe" %% "circe-generic" % "0.14.6",
  "io.circe" %% "circe-parser" % "0.14.6"
)

val backendDependencies = Seq(
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
  "org.scalatest" %% "scalatest" % "3.2.18" % Test
)

// Define dependencies needed for the devServer project
val devServerDependencies = Seq(
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "org.http4s" %% "http4s-ember-server" % "0.23.26",
  "org.http4s" %% "http4s-dsl" % "0.23.26",
  "com.comcast" %% "ip4s-core" % "3.5.0", // Added ip4s dependency
  "ch.qos.logback" % "logback-classic" % "1.5.6" // Added logger
)

// Define custom tasks
lazy val testAll = taskKey[Unit](
  "Run all tests, including backend unit tests and frontend browser tests"
)

lazy val root = project
  .in(file("."))
  .aggregate(backend, frontend, devServer) // Added devServer to aggregation
  .settings(
    name := "yeye",

    // Custom task to run all tests (backend unit tests and frontend browser tests)
    testAll := {
      // First run backend tests
      (backend / Test / test).value
      // Then run frontend tests
      (frontend / Test / test).value
    }
  )

lazy val backend = project
  .in(file("backend"))
  .settings(
    name := "yeye-backend",
    libraryDependencies ++= backendDependencies,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:postfixOps"
    ),
    Compile / mainClass := Some("com.yeye.backend.Server"),
    run / fork := true,
    run / javaOptions ++= Seq(
      "-Dlogback.configurationFile=logback.xml",
      "-Xmx1G"
    )
  )
  .dependsOn(shared)

// Define the new devServer project
lazy val devServer = project
  .in(file("devServer"))
  .settings(
    name := "yeye-devserver",
    libraryDependencies ++= devServerDependencies,
    Compile / mainClass := Some("com.yeye.devserver.DevServer"),
    run / fork := true
  )
  .dependsOn(shared) // Assuming devServer might need shared code, adjust if not

lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "yeye-frontend",
    // Specify Main as the main class to avoid conflicts with test classes
    Compile / mainClass := Some("com.yeye.frontend.Main"),
    scalaJSUseMainModuleInitializer := true,

    // Define how to run frontend tests as part of the standard test task
    Test / test := {
      import scala.sys.process._

      // Compile first - important: we need to use fastOptJS, not fastLinkJS
      (Compile / fastOptJS).value

      // Run the script in headless mode for automation
      println("Running frontend browser tests in headless mode...")
      val scriptPath = baseDirectory.value / "run-tests.sh"

      // Make sure the script is executable
      s"chmod +x ${scriptPath}".!

      // Run with headless flag
      val exitCode = s"${scriptPath} --headless".!

      if (exitCode != 0) {
        throw new Exception(s"Frontend tests failed with exit code $exitCode")
      } else {
        println("✅ Frontend tests passed successfully!")
      }
    }
  )
  .dependsOn(shared)

lazy val shared = project
  .in(file("shared"))
  .settings(
    name := "yeye-shared",
    libraryDependencies ++= sharedDependencies
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF")  => MergeStrategy.discard
  case PathList("META-INF", "services", _*) => MergeStrategy.concat
  case PathList("META-INF", _*)             => MergeStrategy.discard
  case _                                    => MergeStrategy.first
}
