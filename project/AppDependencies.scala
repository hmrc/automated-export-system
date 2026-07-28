import sbt.*

object AppDependencies {
  private val bootstrapVersion = "10.8.0"
  private val playVersion      = "play-30"
  private val hmrcMongoVersion = "2.13.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                       % "2.13.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion % Test,
    "org.scalatest"          %% "scalatest"                     % "3.2.20"         % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"            % "7.0.2"          % Test,
    "org.scalacheck"         %% "scalacheck"                    % "1.19.0"         % Test,
    "org.scalatestplus"      %% "scalacheck-1-19"               % "3.2.20.0"       % Test
  )
}
