package scan_generator 
import chisel3._
import chisel3.util._
import net.jcazevedo.moultingyaml._
import scala.io.Source
import java.nio.file.{Paths, Files}
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

// TODO Add CLI parser 
// TODO Generate definitions in verilog
// TODO Create a map data structure for accessing cellIn and cellOut in chisel modules
// TODO generate verilog tasks for testing scan chains  
// TODO generate timing sdc commands   

/** Scan Chain 
 *  Description: Connects scan chain cells to form scan chain elements 
 *  @param p ScanCellParameters. See defined case class
 *  */
class ScanChainGenerator(p: ScanChainParameters) extends RawModule {
  
  //-------------------------------------------------------
  // Create all cells
  //-------------------------------------------------------
  private val cells: ListBuffer[ScanCell] = ListBuffer() 
  private val nameToIdOut: Map[String,Int] = Map()
  private val nameToIdIn: Map[String,Int] = Map()
  private var readCtr = 0
  private var writeCtr = 0
  p.cells.foreach(c => {
    // Iterate over mult
    for (i <- 0 until c.mult) {
      // Populate name to Id maps 
      val name = s"${c.name}_$i"
      if (c.write) { 
        require(!nameToIdOut.contains(name),s"Multiple write cells named $name")
        nameToIdOut += (name -> writeCtr); writeCtr += 1 
      } else { 
        require(!nameToIdIn.contains(name),s"Multiple read cells named $name")
        nameToIdIn += (name -> readCtr); readCtr += 1 
      }
      // Create cell
      cells += Module(new ScanCell(new ScanCellParameters(
          name = s"$name",
          width=c.width,
          cellType = p.cellType,
          write=c.write
      )))
    }
  })

  //-------------------------------------------------------
  // Calculate IO widths and filter cells
  //-------------------------------------------------------
  val writeCells = cells.filter(x => x.p.write == true)
  val readCells = cells.filter(x => x.p.write == false)
  val outWidth = writeCells.map(x => x.p.width).fold(0)((a,b) => a + b)
  val inWidth = readCells.map(x => x.p.width).fold(0)((a,b) => a + b)
  
  //-------------------------------------------------------
  // IO declaration 
  //-------------------------------------------------------
  val io = IO(new Bundle(){
    val scan = new ScanIOs(p.cellType) 
    val out = if (outWidth != 0) Some(Output(MixedVec(writeCells.map(c => UInt(c.p.width.W))))) else None
    val in = if (inWidth != 0) Some(Input(MixedVec(readCells.map(c => UInt(c.p.width.W))))) else None
  })

  //-------------------------------------------------------
  // Connect all cells
  //-------------------------------------------------------
  // Re-init read and write counters
  readCtr = 0
  writeCtr = 0
  // Connect scan cells
  cells.zipWithIndex.foreach{ case(c,i) => {
    c.io.scan.control <> io.scan.control
    // Connect cell in/out 
    if (c.p.write) {
      io.out.get(writeCtr) := c.io.cellOut.get
      writeCtr += 1 
    } else {
      c.io.cellIn.get := io.in.get(readCtr)
      readCtr += 1 
    }
    // Connect scan in and out
    if (i == 0) { 
      c.io.scan.in := io.scan.in 
    } else {
      c.io.scan.in := cells(i-1).io.scan.out
      if (i == cells.length-1) io.scan.out := c.io.scan.out
    }
  }}

  //-------------------------------------------------------
  // Functions for returning read/in IOs and write/out IOs 
  //-------------------------------------------------------
  /** Returns the out port so you can assign something else to the out value 
   *  @param name name_i where name is name of yml and i is mult number
   *  */
  def outIO(name: String) = {
    io.out.get(nameToIdOut(name))
  }
  
  /** Returns the in port so you can assign to it
   *  @param name name_i where name is name of yml and i is mult number
   *  */
  def inIO(name: String) = {
    io.in.get(nameToIdIn(name))
  }
}
