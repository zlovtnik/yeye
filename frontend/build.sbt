name := "yeye-frontend"
scalaVersion := "3.3.5"

enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  "com.raquo" %%% "laminar" % "16.0.0",
  "com.raquo" %%% "waypoint" % "7.0.0",
  "org.scala-js" %%% "scalajs-dom" % "2.8.0",
  "com.lihaoyi" %%% "upickle" % "3.1.3",
  "io.circe" %%% "circe-core" % "0.14.6",
  "io.circe" %%% "circe-generic" % "0.14.6",
  "io.circe" %%% "circe-parser" % "0.14.6",
  "org.typelevel" %%% "cats-core" % "2.10.0",
  "org.typelevel" %%% "cats-effect" % "3.5.2",
  "org.scalameta" %%% "munit" % "0.7.29" % Test,
  "com.lihaoyi" %%% "utest" % "0.8.1" % Test
)

scalaJSUseMainModuleInitializer := true

Compile / fastLinkJS / scalaJSLinkerConfig ~= {
  _.withModuleKind(ModuleKind.ESModule)
    .withSourceMap(true)
}

// Tailwind CSS build is now handled in the Dockerfile
