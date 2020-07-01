package scan_generator 
import chisel3._
import chisel3.util._

// TODO Flesh out all useful classes that use ScanReadCell as core

/** 2-phase Read Cell */
object TwoPhaseScanReadCell {
  def apply(name: String, width: Int): ScanReadCell = {
    val p = ScanCellParameters(
      name=name,
      width=width,
      cellType = new ScanCellType(twoPhase=true,update=false,updateLatch=None),
      write=false
    ) 
    new ScanReadCell(p)
  }
}

/** ScanReadCell variant of ScanCell */
class ScanReadCell(p: ScanCellParameters) extends RawModule{
  val io = IO(new Bundle(){
    val scan = new ScanIOs(p.cellType)
    val cellIn = Input(UInt(p.width.W))
  })
  require(!p.write,"Write is TRUE for ScanReadCell parameters!")
  val core = Module(new ScanCell(p))
  core.io.scan <> io.scan
  core.io.cellIn.get := io.cellIn
}
