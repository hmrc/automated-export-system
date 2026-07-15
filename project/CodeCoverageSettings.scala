import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "com.kenshoo.play.metrics.*",
    ".*definition.*",
    "prod.*",
    "testOnlyDoNotUseInAppConf.*",
    ".*test.*",
    "app.*",
    ".*BuildInfo.*",
    ".*Routes.*",
    ".*models.*",
    ".*controllers.test.*",
    ".*services.test.*",
    ".*metrics.*"
  )

  val settings: Seq[Setting[?]] = Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    //TODO: update to 90 following implementation of endpoints
    ScoverageKeys.coverageMinimumStmtTotal := 10,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
