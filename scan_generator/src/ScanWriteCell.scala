package scan_generator 
import chisel3._
import chisel3.util._

// TODO Flesh out all useful classes that use ScanWriteCell as core

/** 2-phase Write Cell w/ update */
object TwoPhaseScanWriteCell {
  def apply(name: String, width: Int): ScanWriteCell = {
    val p = ScanCellParameters(name=name,width=width,twoPhase=true,write=true,update=true,updateLatch=Some(true)) 
    new ScanWriteCell(p)
  }
} 

/** ScanWriteCell variant of ScanCell */
class ScanWriteCell(p: ScanCellParameters) extends Module{
  val io = IO(new Bundle(){
    val scanIn = Input(Bool())
    val scanOut = Output(Bool())
    val scanEn = Input(Bool())
    val cellOut = Output(UInt(p.width.W))
    val scanClk = if (!p.twoPhase) Some(Input(Clock())) else None
    val scanClkP = if (p.twoPhase) Some(Input(Clock())) else None
    val scanClkN = if (p.twoPhase) Some(Input(Clock())) else None
    val scanUpdate = if (p.update) Some(Input(Bool())) else None
    val scanReset = if (p.update) Some(Input(Bool())) else None
  })
  require(p.write,"Write is FALSE for ScanWriteCell parameters!")
  val core = Module(new ScanCell(p))
  core.io.scanIn := io.scanIn
  io.scanOut := core.io.scanOut
  core.io.scanEn := io.scanEn
  io.cellOut := core.io.cellOut.get
  if (!p.twoPhase) core.io.scanClk.get := io.scanClk.get 
  if (p.twoPhase) core.io.scanClkP.get := io.scanClkP.get
  if (p.twoPhase) core.io.scanClkN.get := io.scanClkN.get 
  if (p.update) core.io.scanUpdate.get := io.scanUpdate.get 
  if (p.update) core.io.scanReset.get := io.scanReset.get 
}
