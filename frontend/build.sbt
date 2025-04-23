name := "yeye-frontend"
scalaVersion := "3.3.1"

enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  "com.raquo" %%% "laminar" % "16.0.0",
  "org.scala-js" %%% "scalajs-dom" % "2.8.0",
  "com.lihaoyi" %%% "upickle" % "3.1.3",
  "io.circe" %%% "circe-core" % "0.14.6",
  "io.circe" %%% "circe-generic" % "0.14.6",
  "io.circe" %%% "circe-parser" % "0.14.6",
  "org.scalameta" %%% "munit" % "1.0.0-M10" % Test,
  "dev.zio" %%% "zio-json" % "0.6.2",
  "com.lihaoyi" %%% "utest" % "0.8.1" % Test
)

scalaJSUseMainModuleInitializer := true

Compile / fastLinkJS / scalaJSLinkerConfig ~= {
  _.withModuleKind(ModuleKind.ESModule)
    .withSourceMap(true)
}
