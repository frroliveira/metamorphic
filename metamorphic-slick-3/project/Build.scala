import sbt._
import Keys._

object MetamorphicSlick3Build extends Build {

  val thisV = "1.0-SNAPSHOT"

  val metamorphicV = "0.0-SNAPSHOT"
  val paradiseV = "2.1.0-M5"
  val slickV = "3.0.0"
  val jodaMapperV = "2.0.0"
  
  lazy val compileDependencies = Seq(
    "com.typesafe.slick" %% "slick" % slickV,
    "com.github.tototoshi" %% "slick-joda-mapper" % jodaMapperV
  )
  
  lazy val baseSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros"),
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5"),
    resolvers += Resolver.typesafeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseV cross CrossVersion.full)
  )
  
  lazy val metamorphic_dev = ProjectRef(file("../metamorphic"), "metamorphic_dev")
  
  // Core Project
  
  lazy val slickSettings = Seq(
    libraryDependencies ++= compileDependencies
  )
  
  lazy val slick = (project in file(".")).
    settings(baseSettings: _*).
    settings(slickSettings: _*).
    settings(
      version := thisV,
      name := "slick",
      organization := "metamorphic",
      libraryDependencies += "metamorphic" %% "metamorphic" % metamorphicV
    )
  
  lazy val slick_dev = (project in file(".")).
    dependsOn(metamorphic_dev).
    settings(baseSettings: _*).
    settings(slickSettings: _*).
    settings(
      target := target.value / "dev",
      publish := {}
    )
}
