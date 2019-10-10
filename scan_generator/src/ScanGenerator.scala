package scan_generator 
import chisel3._
import chisel3.util._
import net.jcazevedo.moultingyaml._
import scala.io.Source
import java.nio.file.{Paths, Files}
import scala.collection.mutable.ListBuffer


// TODO Add CLI parser 
// TODO Generate definitions in verilog
// TODO Create a map data structure for accessing cellIn and cellOut in chisel modules
// TODO generate verilog tasks for testing scan chains  
// TODO generate timing sdc commands   
// TODO implement mult   

///////////////////////////////////////////////////////////
// Define YAML schema
///////////////////////////////////////////////////////////
/** Define type of scan cells to be used */
case class ScanCellType (
  twoPhase: Boolean,
  update: Boolean,
  updateLatch: Option[Boolean]
)

/** Define instances w/ mult */
case class ScanMultCellInstance (
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
  cells: Seq[ScanMultCellInstance]
)

/** Scan Chain 
 *  Description: Connects scan chain cells to form scan chain elements 
 *  @param p ScanCellParameters. See defined case class
 *  */
class ScanChainGenerator(p: ScanChainParameters) extends RawModule {
  
  ///////////////////////////////////////////////////////
  // Create all cells
  ///////////////////////////////////////////////////////
  private val cells: ListBuffer[ScanCell] = ListBuffer() 
  p.cells.foreach(c => {
    // Iterate over mult
    for (i <- 0 until c.mult) {
      println(s"Created cell: ${c.name}_$i")
      cells += Module(new ScanCell(new ScanCellParameters(
          name = s"${c.name}_$i",
          width=c.width,
          twoPhase=p.cellType.twoPhase,
          write=c.write,
          update=p.cellType.update,
          updateLatch= p.cellType.updateLatch
      )))
    }
  })
  
  ///////////////////////////////////////////////////////
  // Calculate IO widths and filter cells
  ///////////////////////////////////////////////////////
  val writeCells = cells.filter(x => x.p.write == true)
  val readCells = cells.filter(x => x.p.write == false)
  val outWidth = writeCells.map(x => x.p.width).fold(0)((a,b) => a + b)
  val inWidth = readCells.map(x => x.p.width).fold(0)((a,b) => a + b)
    
  ///////////////////////////////////////////////////////
  // IO declaration 
  ///////////////////////////////////////////////////////
  val io = IO(new Bundle(){
    // Scan in and out
    val scanIn = Input(Bool())
    val scanOut = Output(Bool())
    // Parallel in and out
    val out = if (outWidth != 0) Some(Output(MixedVec(writeCells.map(c => UInt(c.p.width.W))))) else None
    val in = if (inWidth != 0) Some(Input(MixedVec(readCells.map(c => UInt(c.p.width.W))))) else None
    // Reset - resets the output stage only
    val scanReset = if (p.cellType.update) Some(Input(Bool())) else None
    // Clocks 
    val scanClk = if (!p.cellType.twoPhase) Some(Input(Clock())) else None
    val scanClkP = if (p.cellType.twoPhase) Some(Input(Clock())) else None
    val scanClkN = if (p.cellType.twoPhase) Some(Input(Clock())) else None
    // Control Signals
    val scanEn = Input(Bool())
    val scanUpdate = if (p.cellType.update) Some(Input(Bool())) else None
  })

  ///////////////////////////////////////////////////////
  // Connect all cells
  ///////////////////////////////////////////////////////
  var readCtr = 0
  var writeCtr = 0
  // Connect scan cells
  cells.zipWithIndex.foreach{ case(c,i) => {
    // Connect clocks
    if (p.cellType.twoPhase) {
      c.io.scanClkP.get := io.scanClkP.get
      c.io.scanClkN.get := io.scanClkN.get
    } else {
      c.io.scanClk.get := io.scanClk.get
    }
    // Connect in/out 
    if (c.p.write) {
      io.out.get(writeCtr) := c.io.cellOut.get // SUB-WORD ASSIGNMENT!
      writeCtr += 1 
    } else {
      c.io.cellIn.get := io.in.get(readCtr)
      readCtr += 1 
    }
    // Connect control
    c.io.scanEn := io.scanEn
    if (p.cellType.update) c.io.scanUpdate.get := io.scanUpdate.get
    if (p.cellType.update) c.io.scanReset.get := io.scanReset.get
    // Connect scan in and out
    if (i == 0) { 
      c.io.scanIn := io.scanIn 
    } else {
      c.io.scanIn := cells(i-1).io.scanOut
      if (i == cells.length-1) io.scanOut := c.io.scanOut
    }
  }}
}
