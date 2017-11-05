lazy val commonSettings = Seq(
  organization := "com.github.afiore",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.3",
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )
)

val circeVersion = "0.9.0-M2"

val readme     = "README.md"
val readmePath = file(".") / readme

lazy val core =
  (project in file("core"))
    .settings(commonSettings)
    .settings(name := "underlying-core")

val copyReadme =
  taskKey[File](s"Copy readme file to project root")

lazy val circe =
  (project in file("circe"))
    .settings(commonSettings)
    .settings(
      name := "underlying-circe",
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion
),
      copyReadme := {
        val _      = (tut in Compile).value
        val tutDir = (tutTargetDirectory).value
        val log    = streams.value.log

        log.info(s"Copying ${tutDir / readme} to ${file(".") / readme}")

        IO.copyFile(
          tutDir / readme,
          readmePath
        )
        readmePath
      }
    )
    .dependsOn(core)
    .enablePlugins(TutPlugin)

lazy val root = (project in file(".")).aggregate(core, circe)

