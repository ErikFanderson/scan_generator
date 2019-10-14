package scan_generator 
import chisel3._
import chisel3.util._

/** Basic set of IOs that all scan modules have */ 
class ScanIOs(p: ScanCellType) extends Bundle {
  // Scan in and out
  val in = Input(Bool())
  val out = Output(Bool())
  // Reset - resets the output stage only
  val reset = if (p.update) Some(Input(Bool())) else None
  // Clocks 
  val clk = if (!p.twoPhase) Some(Input(Clock())) else None
  val clkP = if (p.twoPhase) Some(Input(Clock())) else None
  val clkN = if (p.twoPhase) Some(Input(Clock())) else None
  // Control Signals
  val en = Input(Bool())
  val update = if (p.update) Some(Input(Bool())) else None
}

/** Scan Chain Cell 
 *  Description: Used as core for ScanReadCell and ScanWriteCell
 *  @param p ScanCellParameters. See defined ScanCellParameters class for details 
 *  */
 class ScanCell(val p: ScanCellParameters) extends RawModule {
  val io = IO(new Bundle(){
    val scan = new ScanIOs(p.cellType) 
    val cellIn = if (!p.write) Some(Input(UInt(p.width.W))) else None
    val cellOut = if (p.write) Some(Output(UInt(p.width.W))) else None
  })

  /////////////////////////////////////////////////////////
  // Basic requirements 
  /////////////////////////////////////////////////////////
  require(p.width > 0, "Width must be greater than 0.")
  
  /////////////////////////////////////////////////////////
  // Generate latches if two phase
  /////////////////////////////////////////////////////////
  val negLatch = if (p.cellType.twoPhase) Some(Module(Latch(p.width))) else None
  val posLatch = if (p.cellType.twoPhase) Some(Module(Latch(p.width))) else None
  if (p.cellType.twoPhase) {
    // Clocks
    negLatch.get.io.clk := io.scan.clkN.get
    posLatch.get.io.clk := io.scan.clkP.get
    // Connect latches
    if (p.width == 1) {
      if (p.write) negLatch.get.io.d := Mux(io.scan.en,io.scan.in,io.cellOut.get)
      else negLatch.get.io.d := Mux(io.scan.en,io.scan.in,io.cellIn.get)
    } else {
      if (p.write) negLatch.get.io.d := Mux(io.scan.en,Cat(posLatch.get.io.q(p.width-2,0),io.scan.in),io.cellOut.get)
      else negLatch.get.io.d := Mux(io.scan.en,Cat(posLatch.get.io.q(p.width-2,0),io.scan.in),io.cellIn.get)
    }
    posLatch.get.io.d := negLatch.get.io.d 
  }
  
  /////////////////////////////////////////////////////////
  // Generate register if not two phase
  /////////////////////////////////////////////////////////
  val scanReg = if (!p.cellType.twoPhase) {
    withClockAndReset(io.scan.clk.get,false.B.asAsyncReset) {Some(Reg(UInt(p.width.W)))} 
  } else {
    None
  }
  if (!p.cellType.twoPhase) {
    if (p.width == 1) {
      if (p.write) scanReg.get := Mux(io.scan.en,io.scan.in,io.cellOut.get)
      else scanReg.get := Mux(io.scan.en,io.scan.in,io.cellIn.get)
    } else {
      if (p.write) scanReg.get := Mux(io.scan.en,Cat(scanReg.get(p.width-2,0),io.scan.in),io.cellOut.get)
      else scanReg.get := Mux(io.scan.en,Cat(scanReg.get(p.width-2,0),io.scan.in),io.cellIn.get)
    }
  }

  /////////////////////////////////////////////////////////
  // Generate cell out update stage if write cell
  /////////////////////////////////////////////////////////
  if (p.write && p.cellType.update) {
    // Latch output stage
    if (p.cellType.updateLatch.get) {
      val updateLatch = Module(ResetLatch(p.width))
      updateLatch.io.clk := io.scan.update.get.asClock
      updateLatch.io.rst := io.scan.reset.get
      io.cellOut.get := updateLatch.io.q
      if (p.cellType.twoPhase) updateLatch.io.d := posLatch.get.io.q
      else          updateLatch.io.d := scanReg.get }
    // Register output stage 
    else {
      val updateReg = withClockAndReset(io.scan.clk.get,io.scan.reset.get.asAsyncReset) {
        RegInit(0.U(p.width.W)) 
      }
      io.cellOut.get := updateReg
      if (p.cellType.twoPhase) { when (io.scan.update.get) { updateReg := posLatch.get.io.q } }
      else          { when (io.scan.update.get) { updateReg := scanReg.get } } 
    }
  // No output stage
  } else if (p.write) {
    if (p.cellType.twoPhase) io.cellOut.get := posLatch.get.io.q
    else            io.cellOut.get := scanReg.get 
  }

  /////////////////////////////////////////////////////////
  // Scan out
  /////////////////////////////////////////////////////////
  if (p.cellType.twoPhase) {
    io.scan.out := posLatch.get.io.q(p.width-1)
  }
  else {
    io.scan.out := scanReg.get(p.width-1)
  }
}
