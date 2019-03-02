name := "mima-web"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license"))

scalaVersion := "2.12.8"

scalacOptions ++= "-deprecation" :: "unchecked" :: "-feature" :: Nil

val unfilteredVersion = "0.9.1"

libraryDependencies ++= Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.scalatest" %% "scalatest" % "3.0.6" % "test",
  "com.typesafe" %% "mima-reporter" % "0.1.18",
  "org.scala-sbt" %% "io" % "1.2.2",
  "io.argonaut" %% "argonaut-scalaz" % "6.2.2",
  "com.github.xuwei-k" %% "httpz-native" % "0.5.1",
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "org.scalaz" %% "scalaz-core" % "7.2.27"
)

enablePlugins(JavaAppPackaging)
