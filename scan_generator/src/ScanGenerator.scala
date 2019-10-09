package scan_generator 
import chisel3._
import chisel3.util._
import net.jcazevedo.moultingyaml._
import scala.io.Source
import java.nio.file.{Paths, Files}

// TODO Add CLI parser 
// TODO Generate definitions in verilog
// TODO Create a map data structure for accessing cellIn and cellOut in chisel modules

///////////////////////////////////////////////////////////
// Define YAML schema
///////////////////////////////////////////////////////////
/** Define type of scan cells to be used */
case class ScanCellType (
  twoPhase: Boolean,
  update: Boolean,
  updateLatch: Option[Boolean]
)

/** Define individual instances */
case class ScanCellInstance (
  name: String,
  width: Int,
  mult: Int,
  write: Boolean
)

/** Define entire scan chain */
case class ScanChainParameters (
  cellType: ScanCellType,
  output_fname_chain: String,
  output_fname_defs: Option[String],
  cells: Seq[ScanCellInstance]
)

/** Scan Chain 
 *  Description: Connects scan chain cells to form scan chain elements 
 *  @param p ScanCellParameters. See defined case class
 *  */
  class ScanChainGenerator(p: ScanChainParameters) {
    println(s"$p")
  //class ScanChainGenerator(p: ScanChainParameters) extends RawModule {
  //var readCtr = 0
  //var writeCtr = 0
  //inWidth,outWidth = calculateIOWidths(p)
  //val io = IO(new Bundle(){
  //  // Scan in and out
  //  val scanIn = Input(Bool())
  //  val scanOut = Output(Bool())
  //  val out = if (!p.write) Some(Input(UInt(p.width.W))) else None
  //  val cellOut = if (p.write) Some(Output(UInt(p.width.W))) else None
  //  // Reset - resets the output stage only
  //  val scanReset = if (p.update) Some(Input(Bool())) else None
  //  // Clocks 
  //  val scanClk = if (!p.twoPhase) Some(Input(Clock())) else None
  //  val scanClkP = if (p.twoPhase) Some(Input(Clock())) else None
  //  val scanClkN = if (p.twoPhase) Some(Input(Clock())) else None
  //  // Control Signals
  //  val scanEn = Input(Bool())
  //  val scanUpdate = if (p.update) Some(Input(Bool())) else None
  //})

  //def calculateIOWidths(p: ScanChainParameters): (Int,Int) = {
  //  (inWidth,outWidth)
  //} 
 
}
