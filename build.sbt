name := "mima-web"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license"))

// mima does not support Scala 2.11/2.12
scalaVersion := "2.10.6"

scalacOptions ++= "-deprecation" :: "unchecked" :: "-feature" :: Nil

val unfilteredVersion = "0.9.1"

libraryDependencies ++= Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "com.typesafe" %% "mima-reporter" % "0.1.14",
  "org.scala-sbt" %% "io" % "1.0.0-M11",
  "io.argonaut" %% "argonaut-scalaz" % "6.2",
  "com.github.xuwei-k" %% "httpz-native" % "0.5.1",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.scalaz" %% "scalaz-core" % "7.2.12"
)

enablePlugins(JavaAppPackaging)
