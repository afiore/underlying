
lazy val coreDeps = Seq(
 libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
 ),
)
lazy val basicSetting = Seq(
  organization := "com.github.afiore",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11","2.12.3"),
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT" )),
  resolvers += Resolver.jcenterRepo,
  releaseCrossBuild := true
)
lazy val commonSettings = basicSetting ++ coreDeps

val circeVersion = "0.9.0-M1"
lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

val readme     = "README.md"
val readmePath = file(".") / readme

val copyReadme =
  taskKey[File](s"Copy readme file to project root")

lazy val core =
  (project in file("core"))
    .settings(commonSettings)
    .settings(commonSettings ++ Seq(
      libraryDependencies += scalaReflect.value
    ))
    .settings(name := "underlying-core")

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
        val tutDir = tutTargetDirectory.value
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
