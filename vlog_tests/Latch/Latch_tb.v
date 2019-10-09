// Project: Latch
// Author: Erik Anderson
// Date: 08/10/2019

`default_nettype none
`timescale 1ns/1ps

//---------------------------------------------------------
// Module: Latch_tb
//---------------------------------------------------------
module Latch_tb();

// Example parameter declaration
parameter WIDTH = 8;

// Example signal declaration 
reg clk;
reg [WIDTH-1:0] d;
wire [WIDTH-1:0] q;

// Example clock declaration 
initial begin
    clk = 1'b0;
//    forever #(1) clk = !clk;
end

// Example DUT instantiaton
Latch dut (
  .io_clk(clk),
  .io_d(d),
  .io_q(q)
);

integer pass;

// Example Simulation
initial begin
    pass = 1;
    // $dumpfile(<filename>); $dumpvars(<levels>,<mod/var 0>,...,<mod/var N>);
    $dumpfile("waves.vcd");
    $dumpvars();
    // Transparent
    clk = 1'b1;
    d = 1'b1;
    #(1);
    pass &= (q == 1'b1);
    d = 1'b0;
    #(1);
    pass &= (q == 1'b0);
    d = 1'b0;
    // Non-Transparent
    clk = 1'b0;
    d = 1'b1;
    #(1);
    pass &= (q == 1'b0);
    d = 1'b0;
    #(1);
    pass &= (q == 1'b0);
    d = 1'b0;
    #(100);
    $display("#####################");
    if (pass)
        $display("# SIMULATION PASSED #");
    else
        $display("SIMULATION FAILED");
    $display("#####################");
    $finish;
end

endmodule
//---------------------------------------------------------

`default_nettype wire
