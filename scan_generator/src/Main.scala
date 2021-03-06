package scan_generator 
import chisel3._
import scala.io.Source
import java.nio.file.{Paths, Files}
import net.jcazevedo.moultingyaml._

object ScanChainYamlProtocol extends DefaultYamlProtocol {
  implicit val scanCellTypeYamlFormat = yamlFormat3(ScanCellType)
  implicit val scanCellInstanceYamlFormat = yamlFormat4(ScanMultCellInstance)
  implicit val scanChainYamlFormat = yamlFormat4(ScanChainParameters)
}

import ScanChainYamlProtocol._

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
      chisel3.Driver.execute(args,() => new ScanChain(params))
    } else {
      println(s"File ${args(0)} does not exist!")
    }
  }
  System.exit(0)
}
