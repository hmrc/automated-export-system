import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"
  private val playVersion = "play-30"
  private val hmrcMongoVersion = "2.12.0"

  val compile = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-$playVersion"        % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"    % bootstrapVersion     % "test,it",
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion"   % hmrcMongoVersion     % "test,it",

    "org.scalatest"     %% "scalatest"          % "3.2.20" % "test,it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % "test,it",

    "org.scalacheck"    %% "scalacheck"         % "1.19.0" % "test",
    "org.scalatestplus" %% "scalacheck-1-17"    % "3.2.18.0" % "test"
  )

}
