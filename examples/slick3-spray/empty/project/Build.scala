import sbt._
import Keys._

object ExampleBuild extends Build {

  val paradiseV = "2.1.0-M5"
  
  lazy val baseSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros"),
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5"),
    resolvers += Resolver.typesafeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseV cross CrossVersion.full)
  )

  lazy val spray_dev = ProjectRef(file("../../../metamorphic-spray"), "spray_dev")
  lazy val slick_dev = ProjectRef(file("../../../metamorphic-slick-3/sqlite"), "sqlite_dev")

  lazy val example = (project in file(".")).
    dependsOn(conf).
    dependsOn(spray_dev).
    dependsOn(slick_dev).
    settings(baseSettings: _*)

  lazy val conf = (project in file("./conf")).
    settings(
      scalaVersion := "2.11.6",
      unmanagedResourceDirectories in Compile += baseDirectory.value,
      excludeFilter in unmanagedResources := "target"
    )
}
