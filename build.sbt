name := "kafka-test-scala"

version := "0.1"
val v2_13 = "2.12.9"

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  scalaVersion := v2_13,
  scalafmtOnCompile := true,
  libraryDependencies ++= Seq(compilerPlugin("com.softwaremill.neme" %% "neme-plugin" % "0.0.15-SNAPSHOT"))
)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(fs2, monix)

lazy val fs2 = (project in file("fs2"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq("com.ovoenergy" %% "fs2-kafka" % "0.20.1")
  )

lazy val monix = (project in file("monix"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq("io.monix" %% "monix-kafka-1x" % "1.0.0-RC5"),
    dependencyOverrides += "org.apache.kafka" % "kafka" % "2.12.0"
  )
