lazy val commonSettings = Seq(
  organization := "com.itv",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.3",
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )
)

val circeVersion = "0.9.0-M2"

lazy val core =
  (project in file("core"))
    .settings(commonSettings)
      .settings(name := "underlying-core")

lazy val circe =
  (project in file("circe"))
    .settings(commonSettings)
      .settings(
        name := "underlying-circe",
        libraryDependencies ++= Seq(
          "io.circe" %% "circe-core" % circeVersion,
          "io.circe" %% "circe-generic" % circeVersion
        ))
    .dependsOn(core)