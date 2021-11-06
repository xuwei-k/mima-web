name := "mima-web"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license"))

scalaVersion := "2.13.7"

scalacOptions ++= "-deprecation" :: "-unchecked" :: "-feature" :: Nil

val unfilteredVersion = "0.10.4"

libraryDependencies ++= Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.scalatest" %% "scalatest" % "3.2.10" % "test",
  "com.typesafe" %% "mima-core" % "1.0.1",
  "org.scala-sbt" %% "io" % "1.5.1",
  "io.argonaut" %% "argonaut-scalaz" % "6.3.7",
  "com.github.xuwei-k" %% "httpz-native" % "0.8.0",
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)

enablePlugins(JavaAppPackaging)
