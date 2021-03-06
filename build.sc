// Project: scan_generator 
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
      ivy"edu.berkeley.cs::chisel-iotesters:1.3-SNAPSHOT",
      ivy"net.jcazevedo::moultingyaml:0.4.1" 
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
object scan_generator extends CommonModule {
  // Specify main class
  //def mainClass = Some("scan_generator.Main")
}

/** mems_switch_control module */
object verilog_test extends CommonModule {
  def moduleDeps = Seq{scan_generator}
  // Specify main class
  //def mainClass = Some("scan_generator.Main")
}
