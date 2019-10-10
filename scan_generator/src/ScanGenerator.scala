package scan_generator 
import chisel3._
import chisel3.util._
import net.jcazevedo.moultingyaml._
import scala.io.Source
import java.nio.file.{Paths, Files}

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
  //class ScanChainGenerator(p: ScanChainParameters) extends RawModule {
  class ScanChainGenerator(p: ScanChainParameters) {
    // Calculate IO widths and filter cells
    val writeCells = p.cells.filter(x => x.write == true)
    val readCells = p.cells.filter(x => x.write == false)
    val outWidth = writeCells.map(x => x.width).fold(0)((a,b) => a + b)
    val inWidth = readCells.map(x => x.width).fold(0)((a,b) => a + b)
    
    //// IO declaration 
    //val io = IO(new Bundle(){
    //  // Scan in and out
    //  val scanIn = Input(Bool())
    //  val scanOut = Output(Bool())
    //  // Parallel in and out
    //  val out = if (outWidth != 0) Some(Output(UInt(outWidth.W))) else None
    //  val in = if (inWidth != 0) Some(Input(UInt(inWidth.W))) else None
    //  // Reset - resets the output stage only
    //  val scanReset = if (p.cellType.scanupdate) Some(Input(Bool())) else None
    //  // Clocks 
    //  val scanClk = if (!p.cellType.twoPhase) Some(Input(Clock())) else None
    //  val scanClkP = if (p.cellType.twoPhase) Some(Input(Clock())) else None
    //  val scanClkN = if (p.cellType.twoPhase) Some(Input(Clock())) else None
    //  // Control Signals
    //  val scanEn = Input(Bool())
    //  val scanUpdate = if (p.cellType.update) Some(Input(Bool())) else None
    //})

    // Create all cells
    val cells = p.cells.map(c => {
      // Iterate over mult
      for (i <- 0 until c.mult) { 
        println(s"Created cell: ${c.name}, mult: $i")
        //Module(new ScanCell(new ScanCellParameters(
        //    width=c.width,
        //    twoPhase=p.cellType.twoPhase,
        //    write=c.write,
        //    update=p.cellType.update,
        //    updateLatch= p.cellType.updateLatch
        //  )))
      }
    })
    
    //var readCtr = 0
    //var writeCtr = 0
    //// Connect scan cells
    //p.cells.foreach(c => {
    //  // Iterate over mult
    //  
    //  // Connect clocks
    //  if (p.cellType.twoPhase) {
    //    cell.io.scanClkP.get := io.scanClkP.get
    //    cell.io.scanClkN.get := io.scanClkN.get
    //  } else {
    //    cell.io.scanClk := io.scanClk.get
    //  }
    //  // Connect in/out 
    //  if (c.write) {
    //    io.out(writeCtr+c.width-1,writeCtr) := cell.io.cellOut.get 
    //    writeCtr += c.width
    //  } else {
    //    cell.io.cellIn.get := io.in(writeCtr+c.width-1,writeCtr)
    //    readCtr += c.width
    //  }
    //  // Connect control
    //  
    //}) 
    
  
}
