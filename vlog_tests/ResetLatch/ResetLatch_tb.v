// Project: ResetLatch
// Author: Erik Anderson
// Date: 08/10/2019

`default_nettype none
`timescale 1ns/1ps

//---------------------------------------------------------
// Module: ResetLatch_tb
//---------------------------------------------------------
module ResetLatch_tb();

// Example parameter declaration
parameter WIDTH = 8;

// Example signal declaration 
reg clk, rst;
reg [WIDTH-1:0] d;
wire [WIDTH-1:0] q;

// Example clock declaration 
initial clk = 1'b0;
initial rst = 1'b0;

// Example DUT instantiaton
ResetLatch dut (
  .io_clk(clk),
  .io_rst(rst),
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
    // Transparent
    clk = 1'b1;
    d = 1'b1;
    #(1);
    pass &= (q == 1'b1);
    // Transparent but reset
    rst = 1'b1;
    #(1);
    pass &= (q == 1'b0);
    // Transparent
    rst = 1'b0;
    clk = 1'b1;
    d = 1'b1;
    #(1);
    pass &= (q == 1'b1);
    // Nontransparent 
    clk = 1'b0;
    #(1);
    pass &= (q == 1'b1);
    // Nontransparent but reset
    rst = 1'b1;
    #(1);
    pass &= (q == 1'b0);

    #(100);
    $display("#####################");
    if (pass)
        $display("# SIMULATION PASSED #");
    else
        $display("# SIMULATION FAILED #");
    $display("#####################");
    $finish;
end

endmodule
//---------------------------------------------------------

`default_nettype wire
