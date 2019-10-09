package mems_switch_control 
import chisel3._
import chisel3.util._
import scala.math._
import chisel3.experimental.ChiselEnum

/* Define Handshake state enum */
object HandshakeState extends ChiselEnum {
  val AckLow, AckHigh, CmdValidAckLow, WaitForReadyAckLow, CmdValidAckHigh, WaitForReadyAckHigh = Value
}
import HandshakeState._

object CMD extends ChiselEnum {
  val CAL_SU, CAL_SD, SWEEP_UP, SWEEP_DOWN, SU_SD, LOCK, MIN_POWER_TRACK, NONE =
    Value
}

/** Chain of registers for synchronizing 
  * @param initialValue the reset value for the sync registers
  * @param length the number of chained register
  */
class Synchronizer(initialValue: Boolean = false, length: Int = 2) extends Module {
  /* Define IO */
  val io = IO(new Bundle(){
    val in = Input(Bool())
    val out = Output(Bool())
  })
  /* Define registers */
  val registers = (0 until length).map(i => RegInit(initialValue.B))
  /* Connect registers and IO */
  registers.zipWithIndex.foreach {
    case (reg, ind) =>
      if (ind == 0) {
        reg := io.in 
      } else {
        reg := registers(ind - 1)
      }
  }
  io.out := registers(length-1)
}

/** Implements 2-phase handshaking and read-valid interface to main tuning FSM */
class HandshakeFsm(sim: Boolean = false) extends Module {
  /* Define IO */
  val io = IO(new Bundle(){
    /* Req/Ack */
    val cmd = Input(CMD()) 
    val req = Input(Bool()) 
    val ack = Output(Bool()) 
    /* Ready/Valids */
    val cmdOut = Decoupled(CMD()) 
    val busy = Flipped(Decoupled(Bool())) 
    /* Simulation */
    val state = if (sim) Some(Output(HandshakeState())) else None
  })
  /* Define registers for state and registered outputs */
  val state = RegInit(AckLow)
  val ack = RegInit(false.B) 
  val cmdValid = RegInit(false.B) 
  val busyReady = RegInit(false.B)
  /* Instantiate synchronizer for incoming REQ signal */
  val req = Module(new Synchronizer())
  req.io.in := io.req
  /* Define state machine */
  switch(state) {
    /* Ack Low */
    is(AckLow) {
      ack := false.B
      cmdValid := false.B
      busyReady := true.B
      when (req.io.out) { state := CmdValidAckLow }
    }
    is(CmdValidAckLow) {
      ack := false.B
      cmdValid := true.B
      busyReady := true.B
      when (io.busy.valid && io.busy.bits) { state := WaitForReadyAckLow }
    }
    is(WaitForReadyAckLow) {
      ack := false.B
      cmdValid := false.B
      busyReady := true.B
      when (io.cmdOut.ready) { state := AckHigh }
    }
    /* Ack High */
    is(AckHigh) {
      ack := true.B
      cmdValid := false.B
      busyReady := true.B
      when (!req.io.out) { state := CmdValidAckHigh }
    }
    is(CmdValidAckHigh) {
      ack := true.B
      cmdValid := true.B
      busyReady := true.B
      when (io.busy.valid && io.busy.bits) { state := WaitForReadyAckHigh }
    }
    is(WaitForReadyAckHigh) {
      ack := true.B
      cmdValid := false.B
      busyReady := true.B
      when (io.cmdOut.ready) { state := AckLow }
    }
  }
  /* Connect registered outputs */
  io.ack := ack 
  io.cmdOut.valid := cmdValid
  io.cmdOut.bits := io.cmd
  io.busy.ready := busyReady 
  if (sim) { io.state.get := state }
}
