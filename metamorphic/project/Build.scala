import sbt._
import Keys._

object MetamorphicBuild extends Build {
  
  val thisV = "0.0-SNAPSHOT"
  
  val paradiseV = "2.1.0-M5"
  val jodaTimeV = "2.4"
  val jodaConvertV = "1.6"
  val configV = "1.2.1"
  
  lazy val compileDependencies = Seq(
    "joda-time" % "joda-time" % jodaTimeV,
    "org.joda" % "joda-convert" % jodaConvertV,
    "com.typesafe" % "config" % configV
  )
  
  lazy val reflection = Def.setting {
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  }
  
  lazy val quasiquotes = Def.setting {
    if (scalaVersion.value.startsWith("2.10")) Seq("org.scalamacros" %% "quasiquotes" % paradiseV) else Nil
  }
  
  lazy val baseSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros"),
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5"),
    resolvers += Resolver.typesafeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseV cross CrossVersion.full)
  )
  
  lazy val metamorphicSettings = Seq(
    libraryDependencies += reflection.value,
    libraryDependencies ++= quasiquotes.value,
    libraryDependencies ++= compileDependencies  
  )
  
  lazy val metamorphic = (project in file(".")).
    settings(baseSettings: _*).
    settings(metamorphicSettings: _*).
    settings(
      version := thisV,
      name := "metamorphic",
      organization := "metamorphic"
    )
    
  lazy val metamorphic_dev = (project in file(".")).
    settings(baseSettings: _*).
    settings(metamorphicSettings: _*).
    settings(
      target := target.value / "dev",
      publish := {}
    )
}
