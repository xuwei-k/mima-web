name := "mima-web"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license"))

scalaVersion := "2.12.11"

scalacOptions ++= "-deprecation" :: "-unchecked" :: "-feature" :: Nil

val unfilteredVersion = "0.10.2"

libraryDependencies ++= Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  "com.typesafe" %% "mima-core" % "0.6.1",
  "org.scala-sbt" %% "io" % "1.5.1",
  "io.argonaut" %% "argonaut-scalaz" % "6.3.6",
  "com.github.xuwei-k" %% "httpz-native" % "0.7.0",
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)

enablePlugins(JavaAppPackaging)
