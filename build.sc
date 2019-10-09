// Project: mems_switch_driver
// Author: Erik Anderson
// Date: 10/08/2019

// build.sc
import mill._, scalalib._
import coursier.maven.MavenRepository

/** Common Module trait making additional modules */
trait CommonModule extends ScalaModule {
    // Specify scala version
    def scalaVersion = "2.11.12"
    
    // Specify scala compiler options
    def scalacOptions = Seq("-deprecation")
  
    //// JVM args
    //def forkArgs = Seq("")

    // Enumerate Ivy dependencies
    def ivyDeps = Agg(
      ivy"edu.berkeley.cs::chisel3:3.2-SNAPSHOT",
      ivy"edu.berkeley.cs::chisel-iotesters:1.3-SNAPSHOT"
    )
    
    // Add releases and snapshots repos
    def repositories = super.repositories ++ Seq(
        MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
        MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
    )
    
    // Test suite
    object test extends Tests {
      def ivyDeps = Agg(
        ivy"edu.berkeley.cs::chisel3:3.2-SNAPSHOT",
        ivy"edu.berkeley.cs::chisel-iotesters:1.3-SNAPSHOT"
      )
      def testFrameworks = Seq("org.scalatest.tools.Framework")
    }
}

/** mems_switch_control module */
object mems_switch_control extends CommonModule {
  // Specify main class
  //def mainClass = Some("mems_switch_control.Main")
}
