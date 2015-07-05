import sbt._
import Keys._

object MetamorphicSlickH2Build extends Build {

  val thisV = "1.0-SNAPSHOT"

  val metamorphicSlickV = "1.0-SNAPSHOT"
  val paradiseV = "2.1.0-M5"
  val h2V = "1.3.170"
  
  lazy val h2CompileDependencies = Seq(
    "com.h2database" % "h2" % h2V
  )

  lazy val baseSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros"),
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5"),
    resolvers += Resolver.typesafeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseV cross CrossVersion.full)
  )
  
  lazy val slick_dev = ProjectRef(file("./.."), "slick_dev")
  
  // H2 Project
  
  lazy val h2Settings = Seq(
    libraryDependencies ++= h2CompileDependencies
  )

  lazy val h2 = (project in file(".")).
    settings(baseSettings: _*).
    settings(h2Settings: _*).
    settings(
      version := thisV,
      name := "slick-h2",
      organization := "metamorphic",
      libraryDependencies += "metamorphic" %% "slick" % metamorphicSlickV
    )
  
  lazy val h2_dev = (project in file(".")).
    dependsOn(slick_dev).
    settings(baseSettings: _*).
    settings(h2Settings: _*).
    settings(
      target := target.value / "dev",
      publish := {}
    )
}
