ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.yeye"

// Common settings for all projects
lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds"
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
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "com.github.ghostdogpr" %% "caliban" % "2.5.0",
      "com.github.ghostdogpr" %% "caliban-zio-http" % "2.5.0",
      "org.typelevel" %% "cats-effect" % "3.5.3",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.0",
      "io.getquill" %% "quill-jdbc" % "4.8.0",
      "com.oracle.database.jdbc" % "ojdbc11" % "23.3.0.23.09",
      "com.zaxxer" % "HikariCP" % "5.0.1",
      "org.slf4j" % "slf4j-api" % "2.0.9",
      "ch.qos.logback" % "logback-classic" % "1.4.11"
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
      "dev.zio" %% "zio-http" % "3.0.0-RC2"
    )
  )
  .dependsOn(frontend)

// Root project
lazy val root = project
  .in(file("."))
  .aggregate(backend, frontend, shared, devServer)
  .settings(
    name := "yeye"
  )
