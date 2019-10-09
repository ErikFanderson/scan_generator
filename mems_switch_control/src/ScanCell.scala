package mems_switch_control 
import chisel3._
import chisel3.util._

/** Parameters to ScanCell
 *  @param width Number of flops inside scan chain cell
 *  @param twoPhase Determines whether registers or separate latches will be used to form cell
 *  @param write Determines whether cell is writeable 
 *  @param update Select whether or not the cell will have an update stage blocking it's output
 *  @param updateLatch If "update" is true then this selects whether a register or a latch gates the output
 *  */
case class ScanCellParameters (
  width: Int,
  twoPhase: Boolean,
  write: Boolean,
  update: Boolean,
  updateLatch: Option[Boolean]
)

/** Scan Chain Cell 
 *  Description: Used as core for ScanReadCell and ScanWriteCell
 *  @param p ScanCellParameters. See defined case class
 *  */
 class ScanCell(p: ScanCellParameters) extends RawModule {
  val io = IO(new Bundle(){
    // Scan in and out
    val scanIn = Input(Bool())
    val scanOut = Output(Bool())
    val cellIn = if (!p.write) Some(Input(UInt(p.width.W))) else None
    val cellOut = if (p.write) Some(Output(UInt(p.width.W))) else None
    // Reset - resets the output stage only
    val scanReset = if (p.update) Some(Input(Bool())) else None
    // Clocks 
    val scanClk = if (!p.twoPhase) Some(Input(Clock())) else None
    val scanClkP = if (p.twoPhase) Some(Input(Clock())) else None
    val scanClkN = if (p.twoPhase) Some(Input(Clock())) else None
    // Control Signals
    val scanEn = Input(Bool())
    val scanUpdate = if (p.update) Some(Input(Bool())) else None
  })

  /////////////////////////////////////////////////////////
  // Basic requirements 
  /////////////////////////////////////////////////////////
  require(p.width > 0, "Width must be greater than 0.")
  
  /////////////////////////////////////////////////////////
  // Generate latches if two phase
  /////////////////////////////////////////////////////////
  val negLatch = if (p.twoPhase) Some(Module(Latch(p.width))) else None
  val posLatch = if (p.twoPhase) Some(Module(Latch(p.width))) else None
  if (p.twoPhase) {
    // Clocks
    negLatch.get.io.clk := io.scanClkN.get
    posLatch.get.io.clk := io.scanClkP.get
    // Connect latches
    if (p.width == 1) {
      if (p.write) negLatch.get.io.d := Mux(io.scanEn,io.scanIn,io.cellOut.get)
      else negLatch.get.io.d := Mux(io.scanEn,io.scanIn,io.cellIn.get)
    } else {
      if (p.write) negLatch.get.io.d := Mux(io.scanEn,Cat(posLatch.get.io.q(p.width-2,0),io.scanIn),io.cellOut.get)
      else negLatch.get.io.d := Mux(io.scanEn,Cat(posLatch.get.io.q(p.width-2,0),io.scanIn),io.cellIn.get)
    }
    posLatch.get.io.d := negLatch.get.io.d 
  }
  
  /////////////////////////////////////////////////////////
  // Generate register if not two phase
  /////////////////////////////////////////////////////////
  val scanReg = if (!p.twoPhase) {
    withClockAndReset(io.scanClk.get,false.B.asAsyncReset) {Some(Reg(UInt(p.width.W)))} 
  } else {
    None
  }
  if (!p.twoPhase) {
    if (p.width == 1) {
      if (p.write) scanReg.get := Mux(io.scanEn,io.scanIn,io.cellOut.get)
      else scanReg.get := Mux(io.scanEn,io.scanIn,io.cellIn.get)
    } else {
      if (p.write) scanReg.get := Mux(io.scanEn,Cat(scanReg.get(p.width-2,0),io.scanIn),io.cellOut.get)
      else scanReg.get := Mux(io.scanEn,Cat(scanReg.get(p.width-2,0),io.scanIn),io.cellIn.get)
    }
  }

  /////////////////////////////////////////////////////////
  // Generate cell out update stage if write cell
  /////////////////////////////////////////////////////////
  if (p.write && p.update) {
    // Latch output stage
    if (p.updateLatch.get) {
      val updateLatch = Module(ResetLatch(p.width))
      updateLatch.io.clk := io.scanUpdate.get.asClock
      updateLatch.io.rst := io.scanReset.get
      io.cellOut.get := updateLatch.io.q
      if (p.twoPhase) updateLatch.io.d := posLatch.get.io.q
      else          updateLatch.io.d := scanReg.get }
    // Register output stage 
    else {
      val updateReg = withClockAndReset(io.scanClk.get,io.scanReset.get.asAsyncReset) {
        RegInit(0.U(p.width.W)) 
      }
      io.cellOut.get := updateReg
      if (p.twoPhase) { when (io.scanUpdate.get) { updateReg := posLatch.get.io.q } }
      else          { when (io.scanUpdate.get) { updateReg := scanReg.get } } 
    }
  // No output stage
  } else if (p.write) {
    if (p.twoPhase) io.cellOut.get := posLatch.get.io.q
    else            io.cellOut.get := scanReg.get 
  }

  /////////////////////////////////////////////////////////
  // Scan out
  /////////////////////////////////////////////////////////
  if (p.twoPhase) {
    io.scanOut := posLatch.get.io.q(p.width-1)
  }
  else {
    io.scanOut := scanReg.get(p.width-1)
  }
}
