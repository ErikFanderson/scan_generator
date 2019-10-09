package scan_generator 
import chisel3._
import chisel3.util._
import chisel3.experimental.IntParam

///////////////////////////////////////////////////////////
// Black box latch w/o reset
///////////////////////////////////////////////////////////

/** Basic latch w/o reset */
object Latch {
  def apply(width: Int): Latch = {
    new Latch(width=width)
  }
}

/** Latch wrapper for latch black box */
class Latch(width: Int) extends RawModule {
  val io = IO(new Bundle() {
    val clk = Input(Clock())
    val d = Input(UInt(width.W))
    val q = Output(UInt(width.W))
  })
  
  // Instantiate black box latch
  val latch_bb = Module(new LatchBlackBox(width=width))
  
  // Connect black box latch to wrapper
  latch_bb.io.i_clk := io.clk
  latch_bb.io.i_d := io.d
  io.q := latch_bb.io.o_q
}

/** Black box latch */
class LatchBlackBox(width: Int) 
extends BlackBox(Map("WIDTH" -> IntParam(width)))
with HasBlackBoxInline {
  val io = IO(new Bundle() {
    val i_clk = Input(Clock())
    val i_d = Input(UInt(width.W))
    val o_q = Output(UInt(width.W))
  })

  // Without reset
  setInline("LatchBlackBox.v",
  s"""
  |module LatchBlackBox #(parameter WIDTH=1)(
  |    input wire i_clk,
  |    input wire [WIDTH-1:0] i_d,  
  |    output reg [WIDTH-1:0] o_q  
  |);
  |always @(*) if (i_clk) o_q <= i_d; 
  |endmodule
  """.stripMargin)
}

///////////////////////////////////////////////////////////
// Black box latch w/ reset
///////////////////////////////////////////////////////////

/** Basic latch w/ async reset */
object ResetLatch {
  def apply(width: Int): ResetLatch = {
    new ResetLatch(width=width)
  }
}

/** Latch wrapper for latch black box */
class ResetLatch(width: Int) extends RawModule {
  val io = IO(new Bundle() {
    val clk = Input(Clock())
    val rst = Input(Bool())
    val d = Input(UInt(width.W))
    val q = Output(UInt(width.W))
  })
  
  // Instantiate black box latch
  val latch_bb = Module(new ResetLatchBlackBox(width=width))
  
  // Connect black box latch to wrapper
  latch_bb.io.i_clk := io.clk
  latch_bb.io.i_d := io.d
  io.q := latch_bb.io.o_q
  latch_bb.io.i_rst := io.rst
}

/** Black box latch w/ reset */
class ResetLatchBlackBox(width: Int) 
extends BlackBox(Map("WIDTH" -> IntParam(width)))
with HasBlackBoxInline {
  val io = IO(new Bundle() {
    val i_clk = Input(Clock())
    val i_rst = Input(Bool())
    val i_d = Input(UInt(width.W))
    val o_q = Output(UInt(width.W))
  })

  // With reset
  setInline("ResetLatchBlackBox.v",
    s"""
    |module ResetLatchBlackBox #(parameter WIDTH=1)(
    |    input wire i_clk,
    |    input wire i_rst,
    |    input wire [WIDTH-1:0] i_d,  
    |    output reg [WIDTH-1:0] o_q  
    |);
    |always @(*) begin
      if (i_rst) o_q <= {WIDTH{1'b0}};
      else if (i_clk) o_q <= i_d;
    end
    |endmodule
  """.stripMargin) 
}
