package scan_generator 
import chisel3._
import chisel3.util._

//-------------------------------------------------------
// Scan IO Classes 
//-------------------------------------------------------
/** Control IOs that can be bulk connected to parent modules */
class ScanControlIOs(p: ScanCellType) extends Bundle {
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

/** Basic set of IOs that all scan modules have */ 
class ScanIOs(p: ScanCellType) extends Bundle {
  // Scan in and out
  val in = Input(Bool())
  val out = Output(Bool())
  val control = new ScanControlIOs(p)
}

//-------------------------------------------------------
// Basic scan cell class definition 
//-------------------------------------------------------
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
  
  //-------------------------------------------------------
  // Basic requirements 
  //-------------------------------------------------------
  require(p.width > 0, "Width must be greater than 0.")
  
  //-------------------------------------------------------
  // Aliases
  //-------------------------------------------------------
  val en = io.scan.control.en 
  val update = io.scan.control.update 
  val reset = io.scan.control.reset 
  val clk = io.scan.control.clk 
  val clkP = io.scan.control.clkP 
  val clkN = io.scan.control.clkN
  val scanIn = io.scan.in 
  val scanOut = io.scan.out 

  //-------------------------------------------------------
  // Generate latches if two phase
  //-------------------------------------------------------
  val negLatch = if (p.cellType.twoPhase) Some(Module(Latch(p.width))) else None
  val posLatch = if (p.cellType.twoPhase) Some(Module(Latch(p.width))) else None
  if (p.cellType.twoPhase) {
    // Clocks
    negLatch.get.io.clk := clkN.get
    posLatch.get.io.clk := clkP.get
    // Connect latches
    if (p.width == 1) {
      if (p.write) negLatch.get.io.d := Mux(en,scanIn,io.cellOut.get)
      else negLatch.get.io.d := Mux(en,scanIn,io.cellIn.get)
    } else {
      if (p.write) negLatch.get.io.d := Mux(en,Cat(posLatch.get.io.q(p.width-2,0),scanIn),io.cellOut.get)
      else negLatch.get.io.d := Mux(en,Cat(posLatch.get.io.q(p.width-2,0),scanIn),io.cellIn.get)
    }
    posLatch.get.io.d := negLatch.get.io.q
  }
  
  //-------------------------------------------------------
  // Generate register if not two phase
  //-------------------------------------------------------
  val scanReg = if (!p.cellType.twoPhase) {
    withClockAndReset(clk.get,false.B.asAsyncReset) {Some(Reg(UInt(p.width.W)))} 
  } else {
    None
  }
  if (!p.cellType.twoPhase) {
    if (p.width == 1) {
      if (p.write) scanReg.get := Mux(en,scanIn,io.cellOut.get)
      else scanReg.get := Mux(en,scanIn,io.cellIn.get)
    } else {
      if (p.write) scanReg.get := Mux(en,Cat(scanReg.get(p.width-2,0),scanIn),io.cellOut.get)
      else scanReg.get := Mux(en,Cat(scanReg.get(p.width-2,0),scanIn),io.cellIn.get)
    }
  }

  //-------------------------------------------------------
  // Generate cell out update stage if write cell
  //-------------------------------------------------------
  if (p.write && p.cellType.update) {
    // Latch output stage
    if (p.cellType.updateLatch.get) {
      val updateLatch = Module(ResetLatch(p.width))
      updateLatch.io.clk := update.get.asClock
      updateLatch.io.rst := reset.get
      io.cellOut.get := updateLatch.io.q
      if (p.cellType.twoPhase) updateLatch.io.d := posLatch.get.io.q
      else updateLatch.io.d := scanReg.get }
    // Register output stage 
    else {
      val updateReg = withClockAndReset(clk.get,reset.get.asAsyncReset) {
        RegInit(0.U(p.width.W)) 
      }
      io.cellOut.get := updateReg
      if (p.cellType.twoPhase) { when (update.get) { updateReg := posLatch.get.io.q } }
      else          { when (update.get) { updateReg := scanReg.get } } 
    }
  // No output stage
  } else if (p.write) {
    if (p.cellType.twoPhase) io.cellOut.get := posLatch.get.io.q
    else            io.cellOut.get := scanReg.get 
  }

  //-------------------------------------------------------
  // Scan out
  //-------------------------------------------------------
  if (p.cellType.twoPhase) {
    scanOut := posLatch.get.io.q(p.width-1)
  }
  else {
    scanOut := scanReg.get(p.width-1)
  }
}
