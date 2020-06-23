ThisBuild / organization := "toy.io"

lazy val root = project
  .in(file("."))
  .settings(
    name := "tio",
    description := "Toy IO Monad",
    version := "0.1.0",
    scalaVersion := "0.25.0-RC1",
    libraryDependencies += "org.specs2" % "specs2-core_2.13" % "4.9.4" % Test
  )


