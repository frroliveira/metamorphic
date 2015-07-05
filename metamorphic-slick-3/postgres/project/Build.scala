import sbt._
import Keys._

object MetamorphicSlickPostgresBuild extends Build {

  val thisV = "1.0-SNAPSHOT"

  val metamorphicSlickV = "1.0-SNAPSHOT"
  val paradiseV = "2.1.0-M5"
  val postgresV = "9.3-1103-jdbc4"
  
  lazy val postgresCompileDependencies = Seq(
    "org.postgresql" % "postgresql" % postgresV
  )

  lazy val baseSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros"),
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5"),
    resolvers += Resolver.typesafeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseV cross CrossVersion.full)
  )
  
  lazy val slick_dev = ProjectRef(file("./.."), "slick_dev")
  
  // Postgres Project
  
  lazy val postgresSettings = Seq(
    libraryDependencies ++= postgresCompileDependencies
  )

  lazy val postgres = (project in file(".")).
    settings(baseSettings: _*).
    settings(postgresSettings: _*).
    settings(
      version := thisV,
      name := "slick-postgres",
      organization := "metamorphic",
      libraryDependencies += "metamorphic" %% "slick" % metamorphicSlickV
    )
  
  lazy val postgres_dev = (project in file(".")).
    dependsOn(slick_dev).
    settings(baseSettings: _*).
    settings(postgresSettings: _*).
    settings(
      target := target.value / "dev",
      publish := {}
    )
}
