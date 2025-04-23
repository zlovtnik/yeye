name := "yeye-backend"
scalaVersion := "3.3.1"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.0.21",
  "dev.zio" %% "zio-http" % "3.0.0-RC4",
  "io.getquill" %% "quill-jdbc-zio" % "4.8.0",
  "com.github.ghostdogpr" %% "caliban" % "2.5.1",
  "com.oracle.database.jdbc" % "ojdbc11" % "23.3.0.23.09",
  "io.circe" %% "circe-core" % "0.14.6",
  "io.circe" %% "circe-generic" % "0.14.6",
  "io.circe" %% "circe-parser" % "0.14.6",
  "dev.zio" %% "zio-test" % "2.0.21" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.0.21" % Test,
  "dev.zio" %% "zio-test-magnolia" % "2.0.21" % Test
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF")  => MergeStrategy.discard
  case PathList("META-INF", "services", _*) => MergeStrategy.concat
  case PathList("META-INF", _*)             => MergeStrategy.discard
  case _                                    => MergeStrategy.first
}
