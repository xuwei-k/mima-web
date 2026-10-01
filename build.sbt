name := "mima-web"

licenses += ("MIT License" -> uri("http://www.opensource.org/licenses/mit-license"))

scalaVersion := "2.13.18"

scalacOptions ++= "-deprecation" :: "-unchecked" :: "-feature" :: Nil

Test / fork := true

val unfilteredVersion = "0.12.1"

libraryDependencies ++= Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.scalatest" %% "scalatest" % "3.2.20" % "test",
  "com.typesafe" %% "mima-core" % "1.2.1",
  "org.scala-sbt" %% "io" % "1.13.4",
  "io.github.argonaut-io" %% "argonaut-scalaz" % "6.3.13",
  "com.github.xuwei-k" %% "httpz-native" % "0.8.0",
  "io.get-coursier" %% "coursier" % "2.1.26"
)

enablePlugins(JavaAppPackaging)
