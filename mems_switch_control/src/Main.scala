package mems_switch_control 
import chisel3._

/** Elaborates Rx and Tx Top in a Top module */
object Main extends App {
  //val myArgs = Array("--top-name","Latch")
  // Generate Latch 
  chisel3.Driver.execute(args,() => Latch(width=8))
}
