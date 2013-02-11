import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "computer-database"
    val appVersion      = "1.0"

    val appDependencies = Seq(
    	jdbc,
    	anorm
    )
    
    val angularFrontend = Project("angular-frontend", file("../angular-frontend"))

    val main = play.Project(appName, appVersion, appDependencies).settings(
      scalaVersion := "2.10.0"      // Add your own project settings here
    ).dependsOn(angularFrontend)


}

