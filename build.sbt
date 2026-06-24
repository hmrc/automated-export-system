import play.sbt.PlayImport.PlayKeys.playDefaultPort
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{integrationTestSettings, scalaSettings}

val appName = "automated-export-system"

lazy val microservice = Project(appName, file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    ScoverageKeys.coverageExcludedFiles := "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;.*test.*;" +
      "app.*;.*BuildInfo.*;.*Routes.*;.*models.*;.*controllers.test.*;.*services.test.*;.*metrics.*",
    ScoverageKeys.coverageMinimumStmtTotal := 10, //TODO: update to 90 following implementation of endpoints
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    playDefaultPort := 5000,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
  )
  .settings(scalaSettings: _*)
  .settings(scalaVersion := "3.5.0")
  .settings(integrationTestSettings(): _*)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    scalacOptions += "-Wconf:src=routes/.*:s", // Silence all warnings in generated routes
    scalacOptions += "-language:postfixOps"
  )
  .settings(
    addCommandAlias("runTestOnly", "run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes")
  )
