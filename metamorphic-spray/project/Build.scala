import sbt._
import Keys._

object MetamorphicSprayBuild extends Build {

  val thisV = "0.0-SNAPSHOT"
  
  val metamorphicV = "0.0-SNAPSHOT"
  val paradiseV = "2.1.0-M5"
  val sprayV = "1.3.2"
  val akkaV = "2.3.6"
  val json4sV = "3.2.11"
  val log4jV = "2.0-rc1"
    
  lazy val compileDependencies = Seq(
    "io.spray" %% "spray-can" % sprayV,
    "io.spray" %% "spray-routing" % sprayV,
    "org.json4s" %% "json4s-ext" % json4sV,
    "org.json4s" %% "json4s-native" % json4sV,
    "org.json4s" %% "json4s-jackson" % json4sV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "org.apache.logging.log4j" % "log4j-api" % log4jV,
    "org.apache.logging.log4j" % "log4j-core" % log4jV,
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jV
  )
  
  lazy val baseSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros"),
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5"),
    resolvers += Resolver.typesafeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseV cross CrossVersion.full)
  )
  
  lazy val metamorphic_dev = ProjectRef(file("../metamorphic"), "metamorphic_dev")
  
  // Spray Project
  
  lazy val spraySettings = Seq(
    libraryDependencies ++= compileDependencies
  )
  
  lazy val spray = (project in file(".")).
    settings(baseSettings: _*).
    settings(spraySettings: _*).
    settings(
      version := thisV,
      name := "spray",
      organization := "metamorphic",
      libraryDependencies += "metamorphic" %% "metamorphic" % metamorphicV
    )
  
  lazy val spray_dev = (project in file(".")).
    dependsOn(metamorphic_dev).
    settings(baseSettings: _*).
    settings(spraySettings: _*).
    settings(
      target := target.value / "dev",
      publish := {}
    )
}
