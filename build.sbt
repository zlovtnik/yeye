ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.yeye"

// Add assembly plugin
ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*)       => MergeStrategy.last
  case PathList("META-INF", "io.netty.versions.properties") =>
    MergeStrategy.last
  case x => MergeStrategy.first
}

// Common settings for all projects
lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-Xlog-implicits",
    "-Xlog-reflective-calls"
  )
)

// Shared module (code used by both backend and frontend)
lazy val shared = project
  .in(file("shared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    name := "yeye-shared",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.10.0",
      "dev.zio" %%% "zio" % "2.0.21",
      "dev.zio" %%% "zio-json" % "0.6.2"
    )
  )

// Backend module
lazy val backend = project
  .in(file("backend"))
  .settings(commonSettings)
  .settings(
    name := "yeye-backend",
    assembly / mainClass := Some("com.yeye.backend.Server"),
    assembly / assemblyJarName := "backend.jar",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.21",
      "dev.zio" %% "zio-http" % "3.0.0-RC4",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.0",
      "io.getquill" %% "quill-jdbc" % "4.8.0",
      "com.oracle.database.jdbc" % "ojdbc11" % "23.3.0.23.09",
      "com.zaxxer" % "HikariCP" % "5.0.1",
      "org.slf4j" % "slf4j-api" % "2.0.9",
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "com.github.ghostdogpr" %% "caliban" % "2.5.1",
      "com.github.ghostdogpr" %% "caliban-zio-http" % "2.5.1"
    )
  )
  .dependsOn(shared)

// Frontend module
lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    name := "yeye-frontend",
    scalaJSUseMainModuleInitializer := true,
    // Add verbose logging for Scala.js
    scalaJSLinkerConfig ~= { _.withSourceMap(true) },
    scalaJSLinkerConfig ~= { _.withPrettyPrint(true) },
    scalaJSLinkerConfig ~= { _.withClosureCompiler(false) },
    // Add logging for the build process
    logLevel := Level.Debug,
    // Set the output directory for compiled files
    Compile / fastLinkJS / scalaJSLinkerOutputDirectory := (ThisBuild / baseDirectory).value / "frontend" / "dist",
    Compile / fullLinkJS / scalaJSLinkerOutputDirectory := (ThisBuild / baseDirectory).value / "frontend" / "dist",
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "16.0.0",
      "dev.zio" %%% "zio-json" % "0.6.2"
    ),
    Compile / fastLinkJS / scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
    }
  )
  .dependsOn(shared)

// Development server module
lazy val devServer = project
  .in(file("devServer"))
  .settings(commonSettings)
  .settings(
    name := "yeye-dev-server",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "dev.zio" %% "zio-logging" % "2.2.1"
    )
  )
  .dependsOn(frontend)

// Root project
lazy val root = project
  .in(file("."))
  .aggregate(backend, frontend, shared, devServer)
  .settings(
    name := "yeye",
    logLevel := Level.Debug
  )
