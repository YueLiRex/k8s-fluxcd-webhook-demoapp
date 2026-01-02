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
    version := "0.0.1",
    Compile / mainClass := Some("com.example.QuickstartApp"),

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
  ).enablePlugins(JavaAppPackaging, DockerPlugin)

// -----------------------------------------------------------------------------
// dev settings
// -----------------------------------------------------------------------------

lazy val devPackage = project
  // we put the results  in a build folder
  .in(file("build/dev"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    // override the main class and resource directory
    name := "k8s-fluxcd-webhook-demoapp",
    Compile / mainClass := (root / Compile / mainClass).value,
    Compile / resourceDirectory := (root / Compile / resourceDirectory).value,
    Universal / mappings += {
      ((Compile / resourceDirectory).value / "dev.conf") -> "conf/application.conf"
    },
    dockerSettings,
  )
  .dependsOn(root)

lazy val livePackage = project
  // we put the results  in a build folder
  .in(file("build/live"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    // override the main class and resource directory
    name := "k8s-fluxcd-webhook-demoapp",
    Compile / mainClass := (root / Compile / mainClass).value,
    Compile / resourceDirectory := (root / Compile / resourceDirectory).value,
    Universal / mappings += {
      ((Compile / resourceDirectory).value / "live.conf") -> "conf/application.conf"
    },
    dockerSettings,
  )
  .dependsOn(root)


// -----------------------------------------------------------------------------
// Docker settings
// -----------------------------------------------------------------------------

lazy val dockerSettings = Seq(
  dockerRepository := Option("ghcr.io/yuelirex"),
  dockerBaseImage := "ghcr.io/graalvm/graalvm-community:21.0.2",
  dockerPermissionStrategy := DockerPermissionStrategy.Run,
  dockerVersion := Some(DockerVersion(0, 0, 1, None)),
  Docker / packageName := "demoapp",
  Docker / version := version.value,
  dockerExposedPorts ++= Seq(8080),
)
