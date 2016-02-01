import sbt._
import Keys._
import wartremover._

object BuildSettings {
  val buildSettings = Defaults.coreDefaultSettings ++ Seq(
    organization := "warden",
    version := "0.0.0",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture"
    ),
    scalaVersion := "2.11.7",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "org.scalatest.tags.Slow", "-u","target/junit-xml-reports", "-oD", "-eS"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
      "org.scalatest"   %% "scalatest"    % "2.2.4"   % "test",
      "org.scalacheck"  %% "scalacheck"   % "1.12.2"      % "test",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
    ),
    wartremoverErrors in (Compile, compile) ++=  Warts.allBut(
      Wart.IsInstanceOf,
      Wart.AsInstanceOf,
      Wart.Throw,
      Wart.Null,
      Wart.Var,
      Wart.Any,
      Wart.NonUnitStatements
    )
)

}

object WardenBuild extends Build {
  import BuildSettings._

  lazy val root: Project = Project(
    "root",
    file("."),
    settings = buildSettings ++ Seq(
//      run <<= run in Compile in core
    )
  ) aggregate(driver, core, stdlib)

  lazy val driver: Project = Project(
    "driver",
    file("driver"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % "3.3.0"
      ),
      wartremoverErrors in (Compile, compile) :=  Warts.allBut(
        Wart.DefaultArguments,
        Wart.Throw,
        Wart.NonUnitStatements
      )
    )
  ) dependsOn(core)

  lazy val core: Project = Project(
    "core",
    file("core"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.ow2.asm" % "asm-all" % "5.0.4",
        "org.parboiled" %% "parboiled" % "2.1.0"
      )
    )
  ) dependsOn(stdlib)

  lazy val stdlib: Project = Project(
    "stdlib",
    file("stdlib"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.orbitz.consul" % "consul-client" % "0.9.16",
        "org.apache.cxf" % "cxf-rt-rs-client" % "3.0.3",
        "org.apache.cxf" % "cxf-rt-transports-http-hc" % "3.0.3"
      ),
      autoScalaLibrary := false
    )
  )




}