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
class ScanReadCell(p: ScanCellParameters) extends Module{
  val io = IO(new Bundle(){
    val scan = new ScanIOs(p.cellType)
    val cellIn = Input(UInt(p.width.W))
  })
  require(!p.write,"Write is TRUE for ScanReadCell parameters!")
  val core = Module(new ScanCell(p))
  core.io.scan.in := io.scan.in
  io.scan.out := core.io.scan.out
  core.io.scan.en := io.scan.en
  core.io.cellIn.get := io.cellIn
  if (!p.cellType.twoPhase) core.io.scan.clk.get := io.scan.clk.get 
  if (p.cellType.twoPhase) core.io.scan.clkP.get := io.scan.clkP.get
  if (p.cellType.twoPhase) core.io.scan.clkN.get := io.scan.clkN.get 
}
