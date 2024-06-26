//See LICENSE for license details.

package firesim.firesim

import chisel3._
import chisel3.experimental.annotate
import chisel3.experimental.{DataMirror, Direction}
import chisel3.util.experimental.BoringUtils

import org.chipsalliance.cde.config.{Field, Config, Parameters}
import freechips.rocketchip.diplomacy.{LazyModule, CloneLazyModule}
import freechips.rocketchip.devices.debug.{Debug, HasPeripheryDebug}
import freechips.rocketchip.amba.axi4.{AXI4Bundle}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tile.{RocketTile}
import freechips.rocketchip.prci.{ClockBundle, ClockBundleParameters}
import freechips.rocketchip.util.{ResetCatchAndSync}
import sifive.blocks.devices.uart._

import testchipip._
import icenet.{CanHavePeripheryIceNIC, SimNetwork, NicLoopback, NICKey, NICIOvonly}

import junctions.{NastiKey, NastiParameters}
import midas.models.{FASEDBridge, AXI4EdgeSummary, CompleteConfig}
import midas.targetutils.{MemModelAnnotation, EnableModelMultiThreadingAnnotation}
import firesim.bridges._
import firesim.configs.MemModelKey
import tracegen.{TraceGenSystemModuleImp}
import cva6.CVA6Tile

import boom.common.{BoomTile}
import barstools.iocell.chisel._
import chipyard.iobinders.{IOBinders, OverrideIOBinder, ComposeIOBinder, GetSystemParameters, IOCellKey}
import chipyard._
import chipyard.harness._

object MainMemoryConsts {
  val regionNamePrefix = "MainMemory"
  def globalName()(implicit p: Parameters) = s"${regionNamePrefix}_${p(MultiChipIdx)}"
}

trait Unsupported {
  require(false, "We do not support this IOCell type")
}

class FireSimAnalogIOCell extends RawModule with AnalogIOCell with Unsupported {
  val io = IO(new AnalogIOCellBundle)
}
class FireSimDigitalGPIOCell extends RawModule with DigitalGPIOCell with Unsupported {
  val io = IO(new DigitalGPIOCellBundle)
}
class FireSimDigitalInIOCell extends RawModule with DigitalInIOCell {
  val io = IO(new DigitalInIOCellBundle)
  io.i := io.pad
}
class FireSimDigitalOutIOCell extends RawModule with DigitalOutIOCell {
  val io = IO(new DigitalOutIOCellBundle)
  io.pad := io.o
}

case class FireSimIOCellParams() extends IOCellTypeParams {
  def analog() = Module(new FireSimAnalogIOCell)
  def gpio()   = Module(new FireSimDigitalGPIOCell)
  def input()  = Module(new FireSimDigitalInIOCell)
  def output() = Module(new FireSimDigitalOutIOCell)
}

class WithFireSimIOCellModels extends Config((site, here, up) => {
  case IOCellKey => FireSimIOCellParams()
})

class WithTSIBridgeAndHarnessRAMOverSerialTL extends OverrideHarnessBinder({
  (system: CanHavePeripheryTLSerial, th: FireSim, ports: Seq[ClockedIO[SerialIO]]) => {
    ports.map { port =>
      implicit val p = GetSystemParameters(system)
      val bits = port.bits
      port.clock := th.harnessBinderClock
      val ram = TSIHarness.connectRAM(system.serdesser.get, bits, th.harnessBinderReset)
      TSIBridge(th.harnessBinderClock, ram.module.io.tsi, p(ExtMem).map(_ => MainMemoryConsts.globalName), th.harnessBinderReset.asBool)
    }
    Nil
  }
})

class WithNICBridge extends OverrideHarnessBinder({
  (system: CanHavePeripheryIceNIC, th: FireSim, ports: Seq[ClockedIO[NICIOvonly]]) => {
    val p: Parameters = GetSystemParameters(system)
    ports.map { n => NICBridge(n.clock, n.bits)(p) }
    Nil
  }
})

class WithUARTBridge extends OverrideHarnessBinder({
  (system: HasPeripheryUARTModuleImp, th: FireSim, ports: Seq[UARTPortIO]) =>
    val uartSyncClock = Wire(Clock())
    uartSyncClock := false.B.asClock
    val pbusClockNode = system.outer.asInstanceOf[HasTileLinkLocations].locateTLBusWrapper(PBUS).fixedClockNode
    val pbusClock = pbusClockNode.in.head._1.clock
    BoringUtils.bore(pbusClock, Seq(uartSyncClock))
    ports.map { p => UARTBridge(uartSyncClock, p, th.harnessBinderReset.asBool)(system.p) }; Nil
})

class WithBlockDeviceBridge extends OverrideHarnessBinder({
  (system: CanHavePeripheryBlockDevice, th: FireSim, ports: Seq[ClockedIO[BlockDeviceIO]]) => {
    implicit val p: Parameters = GetSystemParameters(system)
    ports.map { b => BlockDevBridge(b.clock, b.bits, th.harnessBinderReset.asBool) }
    Nil
  }
})

class WithAXIOverSerialTLCombinedBridges extends OverrideHarnessBinder({
  (system: CanHavePeripheryTLSerial, th: FireSim, ports: Seq[ClockedIO[SerialIO]]) => {
    implicit val p = GetSystemParameters(system)

    p(SerialTLKey).map({ sVal =>
      val serialTLManagerParams = sVal.serialTLManagerParams.get
      val axiDomainParams = serialTLManagerParams.axiMemOverSerialTLParams.get
      require(serialTLManagerParams.isMemoryDevice)
      val memFreq = axiDomainParams.getMemFrequency(system.asInstanceOf[HasTileLinkLocations])

      ports.map({ port =>
        val axiClock = th.harnessClockInstantiator.requestClockHz("mem_over_serial_tl_clock", memFreq)

        val serial_bits = port.bits
        port.clock := th.harnessBinderClock
        val harnessMultiClockAXIRAM = TSIHarness.connectMultiClockAXIRAM(
          system.serdesser.get,
          serial_bits,
          axiClock,
          ResetCatchAndSync(axiClock, th.harnessBinderReset.asBool))
        TSIBridge(th.harnessBinderClock, harnessMultiClockAXIRAM.module.io.tsi, Some(MainMemoryConsts.globalName), th.harnessBinderReset.asBool)

        // connect SimAxiMem
        (harnessMultiClockAXIRAM.mem_axi4.get zip harnessMultiClockAXIRAM.memNode.get.edges.in).map { case (axi4, edge) =>
          val nastiKey = NastiParameters(axi4.bits.r.bits.data.getWidth,
                                        axi4.bits.ar.bits.addr.getWidth,
                                        axi4.bits.ar.bits.id.getWidth)
          system match {
            case s: BaseSubsystem => FASEDBridge(axi4.clock, axi4.bits, axi4.reset.asBool,
              CompleteConfig(p(firesim.configs.MemModelKey),
                            nastiKey,
                            Some(AXI4EdgeSummary(edge)),
                            Some(MainMemoryConsts.globalName)))
            case _ => throw new Exception("Attempting to attach FASED Bridge to misconfigured design")
          }
        }
      })
    })

    Nil
  }
})

class WithFASEDBridge extends OverrideHarnessBinder({
  (system: CanHaveMasterAXI4MemPortMaxFlight1, th: FireSim, ports: Seq[ClockedAndResetIO[AXI4Bundle]]) => {
    implicit val p: Parameters = GetSystemParameters(system)
    (ports zip system.memAXI4Node.edges.in).map { case (axi4, edge) =>
      val nastiKey = NastiParameters(axi4.bits.r.bits.data.getWidth,
                                     axi4.bits.ar.bits.addr.getWidth,
                                     axi4.bits.ar.bits.id.getWidth)
      system match {
        case s: BaseSubsystem => FASEDBridge(axi4.clock, axi4.bits, axi4.reset.asBool,
          CompleteConfig(p(firesim.configs.MemModelKey),
                         nastiKey,
                         Some(AXI4EdgeSummary(edge)),
                         Some(MainMemoryConsts.globalName)))
        case _ => throw new Exception("Attempting to attach FASED Bridge to misconfigured design")
      }
    }
    Nil
  }
})

class WithTracerVBridge extends ComposeHarnessBinder({
  (system: CanHaveTraceIOModuleImp, th: FireSim, ports: Seq[TraceOutputTop]) => {
    ports.map { p => p.traces.map(tileTrace => TracerVBridge(tileTrace)(system.p)) }
    Nil
  }
})

class WithDromajoBridge extends ComposeHarnessBinder({
  (system: CanHaveTraceIOModuleImp, th: FireSim, ports: Seq[TraceOutputTop]) =>
    ports.map { p => p.traces.map(tileTrace => DromajoBridge(tileTrace)(system.p)) }; Nil
})


class WithTraceGenBridge extends OverrideHarnessBinder({
  (system: TraceGenSystemModuleImp, th: FireSim, ports: Seq[Bool]) =>
    ports.map { p => GroundTestBridge(th.harnessBinderClock, p)(system.p) }; Nil
})

class WithGenericTraceBridge extends ComposeHarnessBinder({
  (system: CanHaveGenericTraceIOModuleImp, th: FireSim, ports: Seq[GenericTraceOutputTop]) => {
    ports.map { p => p.generic_traces.map(tileTrace => GenericTraceBridge(tileTrace)(system.p)) }
    Nil
  }
})

class WithFireSimMultiCycleRegfile extends ComposeIOBinder({
  (system: HasTilesModuleImp) => {
    system.outer.tiles.map {
      case r: RocketTile if (!r.rocketParams.cloneTile) => {
        annotate(MemModelAnnotation(r.module.core.rocketImpl.rf.rf))
        r.module.fpuOpt.foreach(fpu => annotate(MemModelAnnotation(fpu.fpuImpl.regfile)))
      }
      case b: BoomTile if (!b.boomParams.cloneTile) => {
        val core = b.module.backend.core
        core.iregfile match {
          case irf: boom.exu.RegisterFileSynthesizable => annotate(MemModelAnnotation(irf.regfile))
        }
        if (core.fp_pipeline != null) core.fp_pipeline.fregfile match {
          case frf: boom.exu.RegisterFileSynthesizable => annotate(MemModelAnnotation(frf.regfile))
        }
      }
      case _ =>
    }
    (Nil, Nil)
  }
})

class WithFireSimFAME5 extends ComposeIOBinder({
  (system: HasTilesModuleImp) => {
    system.outer.tiles.map {
      case b: BoomTile if (!b.boomParams.cloneTile) =>
        annotate(EnableModelMultiThreadingAnnotation(b.module))
      case r: RocketTile if (!r.rocketParams.cloneTile) =>
        annotate(EnableModelMultiThreadingAnnotation(r.module))
      case _ => Nil
    }
    (Nil, Nil)
  }
})

// Shorthand to register all of the provided bridges above
class WithDefaultFireSimBridges extends Config(
  new WithTSIBridgeAndHarnessRAMOverSerialTL ++
  new WithNICBridge ++
  new WithUARTBridge ++
  new WithBlockDeviceBridge ++
  new WithFASEDBridge ++
  new WithFireSimMultiCycleRegfile ++
  new WithFireSimFAME5 ++
  new WithTracerVBridge ++
  new WithFireSimIOCellModels
)

// Shorthand to register all of the provided mmio-only bridges above
class WithDefaultMMIOOnlyFireSimBridges extends Config(
  new WithTSIBridgeAndHarnessRAMOverSerialTL ++
  new WithUARTBridge ++
  new WithBlockDeviceBridge ++
  new WithFASEDBridge ++
  new WithFireSimMultiCycleRegfile ++
  new WithFireSimFAME5 ++
  new WithFireSimIOCellModels
)

class WithTipTraceFireSimBridges extends Config(
  new WithGenericTraceBridge ++
  new WithDefaultFireSimBridges
)
