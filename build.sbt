name := """stream-backend"""
organization := "org.vivaconagua"

version := "0.0.2"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

//resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers ++= Seq(
  Resolver.sonatypeRepo("public"),
  Resolver.bintrayRepo("scalaz", "releases")
)
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += ehcache
libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += evolutions
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
  "mysql" % "mysql-connector-java" % "8.0.15",

)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test
libraryDependencies += "org.vivaconagua" %% "play2-oauth-client" % "0.4.8-play27"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.8.1"

// Slick MySql
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.vivaconagua.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "org.vivaconagua.binders._"

dockerExposedPorts := Seq(9000, 9443)
dockerRepository := Some("vivaconagua")
routesGenerator := InjectedRoutesGenerator
version in Docker := version.value
