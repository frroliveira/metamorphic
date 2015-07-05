import sbt._
import Keys._

object MetamorphicUnitTestBuild extends Build {

  val metamorphicV = "0.0-SNAPSHOT"
  val paradiseV = "2.1.0-M5"
  val scalaTestV = "2.2.4"
  
  lazy val testDependencies = Seq(
    "org.scalatest" % "scalatest_2.11" % scalaTestV % "test"
  )
  
  lazy val baseSettings = Seq(
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros"),
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5"),
    resolvers += Resolver.typesafeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseV cross CrossVersion.full)
  )
  
  lazy val metamorphic_dev = ProjectRef(file("../metamorphic"), "metamorphic_dev")
  
  lazy val unitTestSettings = Seq(
    libraryDependencies ++= testDependencies
  )
     
  lazy val unit_test = (project in file(".")).
    settings(baseSettings: _*).
    settings(unitTestSettings: _*).
    settings(
      libraryDependencies += "metamorphic" %% "metamorphic" % metamorphicV
    )
    
  lazy val unit_test_dev = (project in file(".")).
    dependsOn(metamorphic_dev).
    settings(baseSettings: _*).
    settings(unitTestSettings: _*).
    settings(
      target := target.value / "dev",
      publish := {}
    )
}
