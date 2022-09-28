name := "mima-web"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license"))

scalaVersion := "2.13.9"

scalacOptions ++= "-deprecation" :: "-unchecked" :: "-feature" :: Nil

val unfilteredVersion = "0.10.4"

libraryDependencies ++= Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.scalatest" %% "scalatest" % "3.2.14" % "test",
  "com.typesafe" %% "mima-core" % "1.1.1",
  "org.scala-sbt" %% "io" % "1.7.0",
  "io.argonaut" %% "argonaut-scalaz" % "6.3.8",
  "com.github.xuwei-k" %% "httpz-native" % "0.8.0",
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)

enablePlugins(JavaAppPackaging)
