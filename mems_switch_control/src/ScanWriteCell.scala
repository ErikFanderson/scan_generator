package mems_switch_control 
import chisel3._
import chisel3.util._
import chisel3.experimental.IntParam
import chisel3.experimental.ChiselEnum
import scala.math._

// 2-phase w/ update
object TwoPhaseScanWriteCell {
  def apply(width: Int): ScanWriteCell = {
    new ScanWriteCell(width=width,twoPhase=true,update=true,updateLatch=Some(true))
  }
} 

/** Scan Chain Cell that can be written to 
 *  @param width Number of flops inside scan chain cell
 *  @param twoPhase Determines whether registers or separate latches will be used to form cell
 *  @param update Select whether or not the cell will have an update stage blocking it's output
 *  @param updateLatch If "update" is true then this selects whether a register or a latch gates the output
 *  @param asyncRst if "update" is true Determines whether or not the reset signal is async
 *  */
class ScanWriteCell(width: Int, twoPhase: Boolean, update: Boolean, updateLatch: Option[Boolean]) extends RawModule {
  val io = IO(new Bundle(){
    // Scan in and out
    val scanIn = Input(Bool())
    val scanOut = Output(Bool())
    val cellOut = Output(Bool())
    // Reset - resets the output stage only
    val scanReset = if (update) Some(Input(Bool())) else None
    // Clocks 
    val scanClk = if (!twoPhase) Some(Input(Clock())) else None
    val scanClkP = if (twoPhase) Some(Input(Clock())) else None
    val scanClkN = if (twoPhase) Some(Input(Clock())) else None
    // Control Signals 
    val scanEn = Input(Bool())
    val scanUpdate = if (update) Some(Input(Bool())) else None
  })

  // Basic requirements 
  require(width > 0, "Width must be greater than 0.")
  
  //
  // Generate latches if two phase
  //
  val negLatch = if (twoPhase) Some(Module(Latch(width))) else None
  val posLatch = if (twoPhase) Some(Module(Latch(width))) else None
  if (twoPhase) {
    // Clocks
    negLatch.get.io.clk := io.scanClkN.get
    posLatch.get.io.clk := io.scanClkP.get
    // Connect latches
    if (width == 1) {
      negLatch.get.io.d := Mux(io.scanEn,io.scanIn,io.cellOut) 
    } else {
      negLatch.get.io.d := Mux(io.scanEn,Cat(posLatch.get.io.q(width-2,0),io.scanIn),io.cellOut) 
    }
    posLatch.get.io.d := negLatch.get.io.d 
  }
  
  //
  // Generate register if not two phase
  //
  val scanReg = if (!twoPhase) {
    withClockAndReset(io.scanClk.get,false.B.asAsyncReset) {Some(Reg(UInt(width.W)))} 
  } else {
    None
  }
  if (!twoPhase) {
    if (width == 1) {
      scanReg.get := Mux(io.scanEn,io.scanIn,io.cellOut)
    } else {
      scanReg.get := Mux(io.scanEn,Cat(scanReg.get(width-2,0),io.scanIn),io.cellOut)
    }
  }

  //
  // Generate cell out update stage
  //
  if (update) {
    // Latch output stage
    if (updateLatch.get) {
      val updateLatch = Module(ResetLatch(width))
      updateLatch.io.clk := io.scanUpdate.get.asClock
      updateLatch.io.rst := io.scanReset.get
      io.cellOut := updateLatch.io.q
      if (twoPhase) updateLatch.io.d := posLatch.get.io.q
      else          updateLatch.io.d := scanReg.get }
    // Register output stage 
    else {
      val updateReg = withClockAndReset(io.scanClk.get,io.scanReset.get.asAsyncReset) {
        RegInit(0.U(width.W)) 
      }
      io.cellOut := updateReg
      if (twoPhase) { when (io.scanUpdate.get) { updateReg := posLatch.get.io.q } }
      else          { when (io.scanUpdate.get) { updateReg := scanReg.get } } 
    }
  // No output stage
  } else {
    if (twoPhase) io.cellOut := posLatch.get.io.q
    else          io.cellOut := scanReg.get 
  }

  //
  // Scan out
  //
  if (twoPhase) {
    io.scanOut := posLatch.get.io.q(width-1)
  }
  else {
    io.scanOut := scanReg.get(width-1)
  }

}
