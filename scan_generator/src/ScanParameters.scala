package scan_generator 
import chisel3._
import chisel3.util._

/** Parameters to ScanCell
 *  @param name name of scan cell - Enables more advanced features
 *  @param width Number of flops inside scan chain cell
 *  @param twoPhase Determines whether registers or separate latches will be used to form cell
 *  @param write Determines whether cell is writeable 
 *  @param update Select whether or not the cell will have an update stage blocking it's output
 *  @param updateLatch If "update" is true then this selects whether a register or a latch gates the output
 *  */
case class ScanCellParameters (
  name: String,
  width: Int,
  write: Boolean,
  cellType: ScanCellType 
)

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
