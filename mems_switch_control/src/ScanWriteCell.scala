package mems_switch_control 
import chisel3._
import chisel3.util._
import chisel3.experimental.IntParam
import scala.math._

///** Latch wrapper for latch black box */
//class Latch(width: Int) extends RawModule {
//  val io = IO(new Bundle() {
//    val clk = Input(Clock())
//    val d = Input(UInt(width.W))
//    val q = Output(UInt(width.W))
//  })
//  // Instantiate black box latch
//  val latch_bb = Module(new LatchBlackBox(width=width))
//  // Connect black box latch to wrapper
//  latch_bb.io.i_clk := io.clk
//  latch_bb.io.i_d := io.d
//  io.q := latch_bb.io.o_q
//}
//
///** Black box latch */
//class LatchBlackBox(width: Int) 
//extends BlackBox(Map("WIDTH" -> IntParam(width)))
////with HasBlackBoxResource {
//with HasBlackBoxInline {
//  val io = IO(new Bundle() {
//    val i_clk = Input(Clock())
//    val i_d = Input(UInt(width.W))
//    val o_q = Output(UInt(width.W))
//  })
//  //addResource("latch.v")
//  setInline("latch.v",
//  s"""
//  |module latch #(WIDTH=1)(
//  |    input wire i_clk,
//  |    input wire [WIDTH-1:0] i_d,  
//  |    output wire [WIDTH-1:0] o_q  
//  |);
//  |always @(clk) if (clk) o_q <= i_d; 
//  |endmodule
//  """.stripMargin)
//}
