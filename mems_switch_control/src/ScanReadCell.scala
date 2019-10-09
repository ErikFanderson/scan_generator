package mems_switch_control 
import chisel3._
import chisel3.util._

// TODO Flesh out all useful classes that use ScanReadCell as core

/** 2-phase Read Cell */
object TwoPhaseScanReadCell {
  def apply(width: Int): ScanReadCell = {
    val p = ScanCellParameters(width=width,twoPhase=true,write=false,update=false,updateLatch=None) 
    new ScanReadCell(p)
  }
} 

/** ScanReadCell variant of ScanCell */
class ScanReadCell(p: ScanCellParameters) extends Module{
  val io = IO(new Bundle(){
    val scanIn = Input(Bool())
    val scanOut = Output(Bool())
    val scanEn = Input(Bool())
    val cellIn = Input(UInt(p.width.W))
    val scanClk = if (!p.twoPhase) Some(Input(Clock())) else None
    val scanClkP = if (p.twoPhase) Some(Input(Clock())) else None
    val scanClkN = if (p.twoPhase) Some(Input(Clock())) else None
  })
  require(!p.write,"Write is TRUE for ScanReadCell parameters!")
  val core = Module(new ScanCell(p))
  core.io.scanIn := io.scanIn
  io.scanOut := core.io.scanOut
  core.io.scanEn := io.scanEn
  core.io.cellIn.get := io.cellIn
  if (!p.twoPhase) core.io.scanClk.get := io.scanClk.get 
  if (p.twoPhase) core.io.scanClkP.get := io.scanClkP.get
  if (p.twoPhase) core.io.scanClkN.get := io.scanClkN.get 
}
