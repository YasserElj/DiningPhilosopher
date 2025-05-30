val scalaVersionUsed = "2.13.12"

lazy val diningPhilosophersLocal = (project in file("."))
  .settings(
    name := "DiningPhilosophersLocal",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scalaVersionUsed,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5", // Using typed for consistency, though actors are classic
      // If we stick to classic actors as before, then akka-actor is sufficient
      // Let's use classic actor dependency: "com.typesafe.akka" %% "akka-actor" % "2.8.5"
      "com.typesafe.akka" %% "akka-actor" % "2.8.5", // Classic actors
      "com.typesafe.akka" %% "akka-slf4j" % "2.8.5",
      "ch.qos.logback" % "logback-classic" % "1.4.14"
    )
  ) 