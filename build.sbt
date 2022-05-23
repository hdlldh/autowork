//val scalaTestArtifact = "org.scalatest" %% "scalatest" % "3.2.+" % Test
val seleniumArtifact = "org.seleniumhq.selenium" % "selenium-java" % "4.1.4"
val slf4jApiArtifact = "org.slf4j" % "slf4j-api" % "1.7.36"
val slf4jSimpleArtifact = "org.slf4j" % "slf4j-simple" % "1.7.36"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint"), // , "-Xfatal-warnings"),
  scalaVersion := "2.13.8",
  libraryDependencies ++= Seq(
//    scalaTestArtifact,
    slf4jApiArtifact,
    slf4jSimpleArtifact
  ),
  fork := true,
  organization := "org.meta.dhu",
  assembly / assemblyMergeStrategy := {
    case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
    case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.first
    case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "autowork",
    libraryDependencies ++= Seq(
      // Add your dependencies here
    )
  ).
  aggregate(tennis)

lazy val tennis = (project in file("autowork-tennis")).
  settings(commonSettings: _*).
  settings(
    name := "autowork-tennis",
    libraryDependencies ++= Seq(
      seleniumArtifact
    )
  )