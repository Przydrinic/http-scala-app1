scalaVersion := "2.13.18"

lazy val root = (project in file("."))
  .settings(
    name := "http-scala-apt",
    libraryDependencies ++= Seq(
      "org.http4s"          %% "http4s-ember-server"       % "0.23.23",
      "com.comcast"         %% "ip4s-core"                 % "3.3.0",
      "org.http4s"          %% "http4s-ember-client"       % "0.23.23",
      "org.http4s"          %% "http4s-dsl"                % "0.23.23",
      "org.http4s"          %% "http4s-circe"              % "0.23.23",


      "org.http4s"          %% "http4s-prometheus-metrics" % "0.24.7",

      "io.prometheus"       %  "simpleclient"              % "0.16.0",
      "io.prometheus"       %  "simpleclient_hotspot"      % "0.16.0",
      "org.typelevel"       %% "log4cats-slf4j"            % "2.6.0",
      "ch.qos.logback"      %  "logback-classic"           % "1.4.14",


      "io.circe"            %% "circe-generic"             % "0.14.6",
      "is.cir"              %% "ciris"                     % "3.2.0",
      "com.disneystreaming" %% "weaver-cats"               % "0.8.3" % Test
    )
  )

testFrameworks += new TestFramework("weaver.framework.CatsEffect")