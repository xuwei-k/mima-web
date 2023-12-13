name := "mima-web"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license"))

scalaVersion := "2.13.12"

scalacOptions ++= "-deprecation" :: "-unchecked" :: "-feature" :: Nil

val unfilteredVersion = "0.12.0"

libraryDependencies ++= Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.scalatest" %% "scalatest" % "3.2.17" % "test",
  "com.typesafe" %% "mima-core" % "1.1.3",
  "org.scala-sbt" %% "io" % "1.9.8",
  "io.argonaut" %% "argonaut-scalaz" % "6.3.9",
  "com.github.xuwei-k" %% "httpz-native" % "0.8.0",
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)

enablePlugins(JavaAppPackaging)
