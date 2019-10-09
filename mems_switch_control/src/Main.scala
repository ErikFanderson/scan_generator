package mems_switch_control 
import chisel3._

/** Elaborates Rx and Tx Top in a Top module */
object Main extends App {
  val myArgs = Array("--top-name","Latch")
  //chisel3.Driver.execute(myArgs,() => new HandshakeFsm())
  chisel3.Driver.execute(myArgs,() => new Latch(width=8))
}
