import com.typesafe.sbt.packager.docker.{DockerChmodType, DockerPermissionStrategy, DockerVersion}

lazy val pekkoHttpVersion = "1.1.0"
lazy val pekkoVersion     = "1.1.2"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.15"
    )),
    name := "k8s-fluxcd-webhook-demoapp",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http"                % pekkoHttpVersion,
      "org.apache.pekko" %% "pekko-http-spray-json"     % pekkoHttpVersion,
      "org.apache.pekko" %% "pekko-actor-typed"         % pekkoVersion,
      "org.apache.pekko" %% "pekko-stream"              % pekkoVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.3.14",

      "org.apache.pekko" %% "pekko-http-testkit"        % pekkoHttpVersion % Test,
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.2.19"         % Test
    ),
    dockerSettings,
    Compile / mainClass := Some("com.example.QuickstartApp")
  ).enablePlugins(JavaAppPackaging, DockerPlugin)

// -----------------------------------------------------------------------------
// Docker settings
// -----------------------------------------------------------------------------

lazy val dockerSettings = Seq(
  dockerRepository := Option("ghcr.io/YueLiRex"),
  dockerBaseImage := "ghcr.io/graalvm/graalvm-community:21.0.2",
  dockerPermissionStrategy := DockerPermissionStrategy.Run,
  dockerVersion := Some(DockerVersion(0, 0, 0, None)),
  Docker / packageName := "demoapp",
  Docker / version := version.value,
  Docker / daemonUserUid := None,
  dockerExposedPorts ++= Seq(8080),
  dockerAdditionalPermissions += (
    DockerChmodType.Custom(
      "+x"
    ),
    s"${(Docker / defaultLinuxInstallLocation).value}/bin/${executableScriptName.value}"
  )
)
