package scan_generator 
import chisel3._
import scala.io.Source
import java.nio.file.{Paths, Files}
import net.jcazevedo.moultingyaml._

object MyYamlProtocol extends DefaultYamlProtocol {
  implicit val scanCellTypeYamlFormat = yamlFormat3(ScanCellType)
  implicit val scanCellInstanceYamlFormat = yamlFormat4(ScanMultCellInstance)
  implicit val scanChainYamlFormat = yamlFormat4(ScanChainParameters)
}

import MyYamlProtocol._

//class ScanTest(p: ScanChainParameters) extends Module {
//  
//  val io = IO(new Bundle(){
//    // Scan in and out
//    val scanIn = Input(Bool())
//    val scanOut = Output(Bool())
//    // Reset - resets the output stage only
//    val scanReset = if (p.cellType.update) Some(Input(Bool())) else None
//    // Clocks 
//    val scanClk = if (!p.cellType.twoPhase) Some(Input(Clock())) else None
//    val scanClkP = if (p.cellType.twoPhase) Some(Input(Clock())) else None
//    val scanClkN = if (p.cellType.twoPhase) Some(Input(Clock())) else None
//    // Control Signals
//    val scanEn = Input(Bool())
//    val scanUpdate = if (p.cellType.update) Some(Input(Bool())) else None
//  })
//  
//  // Make sg
//  val sg = Module(new ScanChainGenerator(p))
//  sg :=  
//}

object MainScanGenerator extends App {
  if (args.length == 0) {
    println("Not enough arguments. Format => scan_generator <filename>.yml ")
  } else {
    if (Files.exists(Paths.get(args(0)))) {
      // Parse YAML
      val bufferedSource = Source.fromFile(args(0))
      val scanYaml = bufferedSource.getLines.mkString("\n")
      bufferedSource.close
      val params = scanYaml.parseYaml.convertTo[ScanChainParameters] 
      // Create scan chain
      //val sg = new ScanChainGenerator(params)
      chisel3.Driver.execute(args,() => new ScanChainGenerator(params))
    } else {
      println(s"File ${args(0)} does not exist!")
    }
  }
  System.exit(0)
}
