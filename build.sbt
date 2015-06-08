name := """Study Buddy"""

version := "0.0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  cache,
  ws,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.github.t3hnar" %% "scala-bcrypt" % "2.4"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

includeFilter in (Assets, LessKeys.less) := "*.css"

