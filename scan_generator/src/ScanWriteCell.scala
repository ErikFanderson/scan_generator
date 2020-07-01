package scan_generator 
import chisel3._
import chisel3.util._

// TODO Flesh out all useful classes that use ScanWriteCell as core

/** 2-phase Write Cell w/ update */
object TwoPhaseScanWriteCell {
  def apply(name: String, width: Int): ScanWriteCell = {
    val p = ScanCellParameters(
      name=name,
      width=width,
      cellType = new ScanCellType(twoPhase=true,update=true,updateLatch=Some(true)),
      write=true
    ) 
    new ScanWriteCell(p)
  }
} 

/** ScanWriteCell variant of ScanCell */
class ScanWriteCell(p: ScanCellParameters) extends RawModule{
  val io = IO(new Bundle(){
    val scan = new ScanIOs(p.cellType)
    val cellOut = Output(UInt(p.width.W))
  })
  require(p.write,"Write is FALSE for ScanWriteCell parameters!")
  val core = Module(new ScanCell(p))
  core.io.scan <> io.scan
  io.cellOut := core.io.cellOut.get
}
