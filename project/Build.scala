import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "computer-database"
    val appVersion      = "1.0"

    val appDependencies = Seq(
    	jdbc,
    	anorm,
        filters
    )
    
    val angularFrontend = Project("angular-frontend", file("angular-frontend")).settings(
      scalaVersion := "2.10.3"      // Add your own project settings here
    )

    override def settings = super.settings ++ Seq(scalaVersion := "2.10.3")
    val main = play.Project("play-backend", appVersion, appDependencies, path = file("play-backend")).settings(
      scalaVersion := "2.10.3"      // Add your own project settings here
    ).dependsOn(angularFrontend)

    override def rootProject = Some(main)


}

