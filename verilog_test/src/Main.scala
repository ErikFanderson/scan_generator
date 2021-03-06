package scan_generator 
import chisel3._

/** Elaborates Rx and Tx Top in a Top module */
object Main extends App {
  //val myArgs = Array("--top-name","Latch")
  
  // Generate Latch 
  chisel3.Driver.execute(args,() => Latch(width=8))
  
  // Generate Latch W/ Reset
  chisel3.Driver.execute(args,() => ResetLatch(width=8))
  
  // Generate Two phase Scan Write Cell 
  chisel3.Driver.execute(args,() => TwoPhaseScanWriteCell(name="scan_write_test",width=8))
  
  // Generate Two phase Scan Write Cell 
  chisel3.Driver.execute(args,() => TwoPhaseScanReadCell(name="scan_read_test",width=8))
}
