// Project: ScanWriteCell
// Author: Erik Anderson
// Date: 16/10/2019

`default_nettype none
`timescale 1ns/1ps

//---------------------------------------------------------
// Module: ScanWriteCell_tb
//---------------------------------------------------------
module ScanWriteCell_tb();

// Example parameter declaration
parameter WIDTH = 8;
parameter real SCAN_CYCLE = 1;
localparam real SCAN_HALF_CYCLE = SCAN_CYCLE/2;

// Example signal declaration 
reg clk, rst;
wire scan_clk_p, scan_clk_n, scan_out;
reg scan_in, scan_en, scan_update, scan_reset;
reg [WIDTH-1:0] out;

// Example clock declaration 
initial begin
    clk = 1'b0;
    forever #(SCAN_HALF_CYCLE) clk = !clk;
end

// Scan Clock Generator
two_phase_clock_generator clk_gen (        
    // global signals
    .clk(clk), 
    .rst(rst),        
    // scan chain clocks
    .scan_clkp(scan_clk_p),
    .scan_clkn(scan_clk_n)
);

// DUT
ScanWriteCell dut (
  .clock(), // Dummy
  .reset(), // Dummy
  .io_scan_in(scan_in),
  .io_scan_out(scan_out),
  .io_scan_control_reset(scan_reset),
  .io_scan_control_clkP(scan_clk_p),
  .io_scan_control_clkN(scan_clk_n),
  .io_scan_control_en(scan_en),
  .io_scan_control_update(scan_update),
  .io_cellOut(out)
);

// Example Simulation
initial begin
    // $dumpfile(<filename>); $dumpvars(<levels>,<mod/var 0>,...,<mod/var N>);
    $dumpfile("waves.vcd");
    $dumpvars();
    // Init signals
    scan_en = 1'b0;
    scan_update = 1'b0;
    scan_reset = 1'b0;
    scan_in = 1'b0;
    // Gen clock 
    rst = 1'b1;
    #(SCAN_CYCLE);
    rst = 1'b0;
    // Test
    #(1000*SCAN_CYCLE);
    $finish;
end

endmodule
//---------------------------------------------------------

`default_nettype wire
