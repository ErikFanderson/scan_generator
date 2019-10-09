package mems_switch_control 
import chisel3._
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import scala.math._
import HandshakeState._

/** Unit test for FlexClockDivider */
class HandshakeTester(c: HandshakeFsm) extends PeekPokeTester(c) {
  private val fsm = c
  /********/
  /* Spec */
  /********/
  idle(ack=false,ready=true,reset=true)
  cmdValid(ack=false)
  waitForReady(ack=false)
  
  idle(ack=true,ready=false,reset=false)
  cmdValid(ack=true)
  waitForReady(ack=true)
  
  idle(ack=false,ready=false,reset=false)
  cmdValid(ack=false)
  waitForReady(ack=false)

  /******************/
  /* Test functions */
  /******************/
  def pokeReqReadyStep(req: Boolean, ready: Boolean, steps: Int): Unit = {
    poke(fsm.io.req, req)
    poke(fsm.io.cmdOut.ready, ready)
    step(steps) // three for req synchronizer
  } 

  def assertStateAckProcess(state: HandshakeState.Type, ack: Boolean, cmdValid: Boolean): Unit = {
    expect(fsm.io.state.get,state)
    //println(s"Expected: $state, received: ${peek(fsm.io.state.get)}")
    expect(fsm.io.ack,ack)
    //println(s"Expected: $ack, received: ${peek(fsm.io.ack)}")
    expect(fsm.io.cmdOut.valid,cmdValid)
    //println(s"Expected: $cmdValid, received: ${peek(fsm.io.cmdOut.valid)}")
  }
 
  /** Waits until reg goes high then transitions */
  def idle(ack: Boolean, ready: Boolean, reset: Boolean): Unit = {
    val state = if (ack) AckHigh else AckLow
    if (!reset) assertStateAckProcess(state,!ack,false) else assertStateAckProcess(AckLow,false,false)
    pokeReqReadyStep(ack,false,1)
    assertStateAckProcess(state,ack,false)
    pokeReqReadyStep(ack,true,1) 
    assertStateAckProcess(state,ack,false)
    pokeReqReadyStep(!ack,ready,3) 
    poke(c.io.busy.valid,0)
    poke(c.io.busy.bits,0)
  }
  
  /** Waits until ready is low and then transitions */
  def cmdValid(ack: Boolean): Unit = {
    val state = if (ack) CmdValidAckHigh else CmdValidAckLow 
    assertStateAckProcess(state,ack,false)
    pokeReqReadyStep(!ack,true,1) 
    assertStateAckProcess(state,ack,true)
    pokeReqReadyStep(!ack,true,1) 
    assertStateAckProcess(state,ack,true)
    poke(c.io.busy.valid,1)
    poke(c.io.busy.bits,1)
    pokeReqReadyStep(!ack,false,1) 
  }
  
  /** Waits until ready is high and then transitions */
  def waitForReady(ack: Boolean): Unit = {
    val state = if (ack) WaitForReadyAckHigh else WaitForReadyAckLow 
    assertStateAckProcess(state,ack,true)
    pokeReqReadyStep(!ack,false,1) 
    assertStateAckProcess(state,ack,false)
    pokeReqReadyStep(!ack,false,1) 
    assertStateAckProcess(state,ack,false)
    pokeReqReadyStep(!ack,true,1) 
  }
}

/** Runs all unittests for Clock Divider blocks */
class HandshakeSpec extends ChiselFlatSpec {
  //private val backendNames = Array("treadle")
  private val backendNames = Array("verilator")
  for (backendName <- backendNames) {
    /* Flex Clock Divider */
    "Two phase handshake receiver fsm" should s"Transition deterministically (with $backendName)" in {
      Driver(() => new HandshakeFsm(sim=true), backendName) { c =>
        new HandshakeTester(c)
      } should be(true)
    }
  }
}
