package chipyard

import chisel3._
import chisel3.util._
import chisel3.experimental.{IO, DataMirror}
import org.chipsalliance.cde.config.{Parameters, Config, Field}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.tile._
import freechips.rocketchip.prci._
import freechips.rocketchip.subsystem.RocketCrossingParams
import freechips.rocketchip.tilelink._
import freechips.rocketchip.rocket._
import firesim.bridges._
import midas.widgets.{Bridge, PeekPokeBridge, RationalClockBridge, RationalClock, ResetPulseBridge, ResetPulseBridgeParameters}
import chipyard.{IsFireChip}


class RocketTileBlackBoxIO extends Bundle {
  val clock =                                    Input(Clock())
  val reset =                                    Input(Bool())
  val auto_int_local_in_3_0 =                    Input(Bool())
  val auto_int_local_in_2_0 =                    Input(Bool())
  val auto_int_local_in_1_0 =                    Input(Bool())
  val auto_int_local_in_1_1 =                    Input(Bool())
  val auto_int_local_in_0_0 =                    Input(Bool())
  val auto_hartid_in =                           Input(UInt(1.W))
  val auto_tl_other_masters_out_a_ready =        Input(Bool())
  val auto_tl_other_masters_out_b_valid =        Input(Bool())
  val auto_tl_other_masters_out_b_bits_opcode =  Input(UInt(3.W))
  val auto_tl_other_masters_out_b_bits_param =   Input(UInt(2.W))
  val auto_tl_other_masters_out_b_bits_size =    Input(UInt(4.W))
  val auto_tl_other_masters_out_b_bits_source =  Input(UInt(2.W))
  val auto_tl_other_masters_out_b_bits_address = Input(UInt(32.W))
  val auto_tl_other_masters_out_b_bits_mask =    Input(UInt(8.W))
  val auto_tl_other_masters_out_b_bits_data =    Input(UInt(64.W))
  val auto_tl_other_masters_out_b_bits_corrupt = Input(Bool())
  val auto_tl_other_masters_out_c_ready =        Input(Bool())
  val auto_tl_other_masters_out_d_valid =        Input(Bool())
  val auto_tl_other_masters_out_d_bits_opcode =  Input(UInt(3.W))
  val auto_tl_other_masters_out_d_bits_param =   Input(UInt(2.W))
  val auto_tl_other_masters_out_d_bits_size =    Input(UInt(4.W))
  val auto_tl_other_masters_out_d_bits_source =  Input(UInt(2.W))
  val auto_tl_other_masters_out_d_bits_sink =    Input(UInt(3.W))
  val auto_tl_other_masters_out_d_bits_denied =  Input(Bool())
  val auto_tl_other_masters_out_d_bits_data =    Input(UInt(64.W))
  val auto_tl_other_masters_out_d_bits_corrupt = Input(Bool())
  val auto_tl_other_masters_out_e_ready =        Input(Bool())
  val auto_wfi_out_0 =                           Output(Bool())
  val auto_tl_other_masters_out_a_valid =        Output(Bool())
  val auto_tl_other_masters_out_a_bits_opcode =  Output(UInt(3.W))
  val auto_tl_other_masters_out_a_bits_param =   Output(UInt(3.W))
  val auto_tl_other_masters_out_a_bits_size =    Output(UInt(4.W))
  val auto_tl_other_masters_out_a_bits_source =  Output(UInt(2.W))
  val auto_tl_other_masters_out_a_bits_address = Output(UInt(32.W))
  val auto_tl_other_masters_out_a_bits_mask =    Output(UInt(8.W))
  val auto_tl_other_masters_out_a_bits_data =    Output(UInt(64.W))
  val auto_tl_other_masters_out_a_bits_corrupt = Output(Bool())
  val auto_tl_other_masters_out_b_ready =        Output(Bool())
  val auto_tl_other_masters_out_c_valid =        Output(Bool())
  val auto_tl_other_masters_out_c_bits_opcode =  Output(UInt(3.W))
  val auto_tl_other_masters_out_c_bits_param =   Output(UInt(3.W))
  val auto_tl_other_masters_out_c_bits_size =    Output(UInt(4.W))
  val auto_tl_other_masters_out_c_bits_source =  Output(UInt(2.W))
  val auto_tl_other_masters_out_c_bits_address = Output(UInt(32.W))
  val auto_tl_other_masters_out_c_bits_data =    Output(UInt(64.W))
  val auto_tl_other_masters_out_c_bits_corrupt = Output(Bool())
  val auto_tl_other_masters_out_d_ready =        Output(Bool())
  val auto_tl_other_masters_out_e_valid =        Output(Bool())
  val auto_tl_other_masters_out_e_bits_sink =    Output(UInt(3.W))
}

class RocketTile_BLACKBOX extends BlackBox with HasBlackBoxResource {
  val io = IO(new RocketTileBlackBoxIO)
  addResource("/vsrc/RocketTileBlackBoxCopy.sv")
}

class RocketTilePRCIDomainIO extends Bundle {
  val auto_int_local_in_3_0 =                    Input(Bool())
  val auto_int_local_in_2_0 =                    Input(Bool())
  val auto_int_local_in_1_0 =                    Input(Bool())
  val auto_int_local_in_1_1 =                    Input(Bool())
  val auto_int_local_in_0_0 =                    Input(Bool())
  val auto_hartid_in =                           Input(UInt(1.W))
  val auto_tl_other_masters_out_a_ready =        Input(Bool())
  val auto_tl_other_masters_out_b_valid =        Input(Bool())
  val auto_tl_other_masters_out_b_bits_opcode =  Input(UInt(3.W))
  val auto_tl_other_masters_out_b_bits_param =   Input(UInt(2.W))
  val auto_tl_other_masters_out_b_bits_size =    Input(UInt(4.W))
  val auto_tl_other_masters_out_b_bits_source =  Input(UInt(2.W))
  val auto_tl_other_masters_out_b_bits_address = Input(UInt(32.W))
  val auto_tl_other_masters_out_b_bits_mask =    Input(UInt(8.W))
  val auto_tl_other_masters_out_b_bits_data =    Input(UInt(64.W))
  val auto_tl_other_masters_out_b_bits_corrupt = Input(Bool())
  val auto_tl_other_masters_out_c_ready =        Input(Bool())
  val auto_tl_other_masters_out_d_valid =        Input(Bool())
  val auto_tl_other_masters_out_d_bits_opcode =  Input(UInt(3.W))
  val auto_tl_other_masters_out_d_bits_param =   Input(UInt(2.W))
  val auto_tl_other_masters_out_d_bits_size =    Input(UInt(4.W))
  val auto_tl_other_masters_out_d_bits_source =  Input(UInt(2.W))
  val auto_tl_other_masters_out_d_bits_sink =    Input(UInt(3.W))
  val auto_tl_other_masters_out_d_bits_denied =  Input(Bool())
  val auto_tl_other_masters_out_d_bits_data =    Input(UInt(64.W))
  val auto_tl_other_masters_out_d_bits_corrupt = Input(Bool())
  val auto_tl_other_masters_out_e_ready =        Input(Bool())

  val auto_wfi_out_0 =                           Output(Bool())
  val auto_tl_other_masters_out_a_valid =        Output(Bool())
  val auto_tl_other_masters_out_a_bits_opcode =  Output(UInt(3.W))
  val auto_tl_other_masters_out_a_bits_param =   Output(UInt(3.W))
  val auto_tl_other_masters_out_a_bits_size =    Output(UInt(4.W))
  val auto_tl_other_masters_out_a_bits_source =  Output(UInt(2.W))
  val auto_tl_other_masters_out_a_bits_address = Output(UInt(32.W))
  val auto_tl_other_masters_out_a_bits_mask =    Output(UInt(8.W))
  val auto_tl_other_masters_out_a_bits_data =    Output(UInt(64.W))
  val auto_tl_other_masters_out_a_bits_corrupt = Output(Bool())
  val auto_tl_other_masters_out_b_ready =        Output(Bool())
  val auto_tl_other_masters_out_c_valid =        Output(Bool())
  val auto_tl_other_masters_out_c_bits_opcode =  Output(UInt(3.W))
  val auto_tl_other_masters_out_c_bits_param =   Output(UInt(3.W))
  val auto_tl_other_masters_out_c_bits_size =    Output(UInt(4.W))
  val auto_tl_other_masters_out_c_bits_source =  Output(UInt(2.W))
  val auto_tl_other_masters_out_c_bits_address = Output(UInt(32.W))
  val auto_tl_other_masters_out_c_bits_data =    Output(UInt(64.W))
  val auto_tl_other_masters_out_c_bits_corrupt = Output(Bool())
  val auto_tl_other_masters_out_d_ready =        Output(Bool())
  val auto_tl_other_masters_out_e_valid =        Output(Bool())
  val auto_tl_other_masters_out_e_bits_sink =    Output(UInt(3.W))
}


class TilePRCIDomain(implicit p: Parameters) extends LazyModule {
  override lazy val module = Module(new TilePRCIDomainImp(this))
}

class TilePRCIDomainImp(outer: TilePRCIDomain)(implicit p: Parameters) extends LazyModuleImp(outer) {
  val io = IO(new RocketTilePRCIDomainIO)

  dontTouch(io)

  val tile = Module(new RocketTile_BLACKBOX())
  tile.io.clock :=                                    clock
  tile.io.reset :=                                    reset.asBool
  tile.io.auto_int_local_in_3_0 :=                    io.auto_int_local_in_3_0
  tile.io.auto_int_local_in_2_0 :=                    io.auto_int_local_in_2_0
  tile.io.auto_int_local_in_1_0 :=                    io.auto_int_local_in_1_0
  tile.io.auto_int_local_in_1_1 :=                    io.auto_int_local_in_1_1
  tile.io.auto_int_local_in_0_0 :=                    io.auto_int_local_in_0_0
  tile.io.auto_hartid_in :=                           io.auto_hartid_in
  tile.io.auto_tl_other_masters_out_a_ready :=        io.auto_tl_other_masters_out_a_ready
  tile.io.auto_tl_other_masters_out_b_valid :=        io.auto_tl_other_masters_out_b_valid
  tile.io.auto_tl_other_masters_out_b_bits_opcode :=  io.auto_tl_other_masters_out_b_bits_opcode
  tile.io.auto_tl_other_masters_out_b_bits_param :=   io.auto_tl_other_masters_out_b_bits_param
  tile.io.auto_tl_other_masters_out_b_bits_size :=    io.auto_tl_other_masters_out_b_bits_size
  tile.io.auto_tl_other_masters_out_b_bits_source :=  io.auto_tl_other_masters_out_b_bits_source
  tile.io.auto_tl_other_masters_out_b_bits_address := io.auto_tl_other_masters_out_b_bits_address
  tile.io.auto_tl_other_masters_out_b_bits_mask :=    io.auto_tl_other_masters_out_b_bits_mask
  tile.io.auto_tl_other_masters_out_b_bits_data :=    io.auto_tl_other_masters_out_b_bits_data
  tile.io.auto_tl_other_masters_out_b_bits_corrupt := io.auto_tl_other_masters_out_b_bits_corrupt
  tile.io.auto_tl_other_masters_out_c_ready :=        io.auto_tl_other_masters_out_c_ready
  tile.io.auto_tl_other_masters_out_d_valid :=        io.auto_tl_other_masters_out_d_valid
  tile.io.auto_tl_other_masters_out_d_bits_opcode :=  io.auto_tl_other_masters_out_d_bits_opcode
  tile.io.auto_tl_other_masters_out_d_bits_param :=   io.auto_tl_other_masters_out_d_bits_param
  tile.io.auto_tl_other_masters_out_d_bits_size :=    io.auto_tl_other_masters_out_d_bits_size
  tile.io.auto_tl_other_masters_out_d_bits_source :=  io.auto_tl_other_masters_out_d_bits_source
  tile.io.auto_tl_other_masters_out_d_bits_sink :=    io.auto_tl_other_masters_out_d_bits_sink
  tile.io.auto_tl_other_masters_out_d_bits_denied :=  io.auto_tl_other_masters_out_d_bits_denied
  tile.io.auto_tl_other_masters_out_d_bits_data :=    io.auto_tl_other_masters_out_d_bits_data
  tile.io.auto_tl_other_masters_out_d_bits_corrupt := io.auto_tl_other_masters_out_d_bits_corrupt
  tile.io.auto_tl_other_masters_out_e_ready :=        io.auto_tl_other_masters_out_e_ready

  io.auto_wfi_out_0 :=                                tile.io.auto_wfi_out_0
  io.auto_tl_other_masters_out_a_valid :=             tile.io.auto_tl_other_masters_out_a_valid
  io.auto_tl_other_masters_out_a_bits_opcode :=       tile.io.auto_tl_other_masters_out_a_bits_opcode
  io.auto_tl_other_masters_out_a_bits_param :=        tile.io.auto_tl_other_masters_out_a_bits_param
  io.auto_tl_other_masters_out_a_bits_size :=         tile.io.auto_tl_other_masters_out_a_bits_size
  io.auto_tl_other_masters_out_a_bits_source :=       tile.io.auto_tl_other_masters_out_a_bits_source
  io.auto_tl_other_masters_out_a_bits_address :=      tile.io.auto_tl_other_masters_out_a_bits_address
  io.auto_tl_other_masters_out_a_bits_mask :=         tile.io.auto_tl_other_masters_out_a_bits_mask
  io.auto_tl_other_masters_out_a_bits_data :=         tile.io.auto_tl_other_masters_out_a_bits_data
  io.auto_tl_other_masters_out_a_bits_corrupt :=      tile.io.auto_tl_other_masters_out_a_bits_corrupt
  io.auto_tl_other_masters_out_b_ready :=             tile.io.auto_tl_other_masters_out_b_ready
  io.auto_tl_other_masters_out_c_valid :=             tile.io.auto_tl_other_masters_out_c_valid
  io.auto_tl_other_masters_out_c_bits_opcode :=       tile.io.auto_tl_other_masters_out_c_bits_opcode
  io.auto_tl_other_masters_out_c_bits_param :=        tile.io.auto_tl_other_masters_out_c_bits_param
  io.auto_tl_other_masters_out_c_bits_size :=         tile.io.auto_tl_other_masters_out_c_bits_size
  io.auto_tl_other_masters_out_c_bits_source :=       tile.io.auto_tl_other_masters_out_c_bits_source
  io.auto_tl_other_masters_out_c_bits_address :=      tile.io.auto_tl_other_masters_out_c_bits_address
  io.auto_tl_other_masters_out_c_bits_data :=         tile.io.auto_tl_other_masters_out_c_bits_data
  io.auto_tl_other_masters_out_c_bits_corrupt :=      tile.io.auto_tl_other_masters_out_c_bits_corrupt
  io.auto_tl_other_masters_out_d_ready :=             tile.io.auto_tl_other_masters_out_d_ready
  io.auto_tl_other_masters_out_e_valid :=             tile.io.auto_tl_other_masters_out_e_valid
  io.auto_tl_other_masters_out_e_bits_sink :=         tile.io.auto_tl_other_masters_out_e_bits_sink
}

class TileOnlyChipTop(implicit p: Parameters) extends LazyModule {
  lazy val lazyTile = LazyModule(p(BuildSystem)(p)).suggestName("TilePRCIDomain")
  lazy val module: LazyModuleImpLike = new LazyRawModuleImp(this) { }

  val implicitClockSourceNode = ClockSourceNode(Seq(ClockSourceParameters(name = Some("top_clock"))))
  val implicitClockSinkNode = ClockSinkNode(Seq(ClockSinkParameters(name = Some("implicit_clock"))))

  implicitClockSinkNode := implicitClockSourceNode

  val tileIO = InModuleBody {
    lazyTile.asInstanceOf[TilePRCIDomain].module match { case l: LazyModuleImp => {
      val implicit_clock = implicitClockSinkNode.in.head._1.clock
      val implicit_reset = implicitClockSinkNode.in.head._1.reset
      l.clock := implicit_clock
      l.reset := implicit_reset

      val tio = IO(DataMirror.internal.chiselTypeClone[RocketTilePRCIDomainIO](l.io))
      l.io <> tio
      tio
    }}
  }

  val clockIO = InModuleBody {
    implicitClockSourceNode.makeIOs()
  }
}

class WithTileOnlyConfig extends Config((site, here, up) => {
  case BuildSystem => (p: Parameters) => new TilePRCIDomain()(p)
  case BuildTop => (p: Parameters) => new TileOnlyChipTop()(p)
})

// This is more of a placeholder right now
class WithTileOnlyFireSimBridges extends Config((site, here, up) => {
  case IsFireChip => true
})

class FireSimRocketTileConfig extends Config(
  new chipyard.WithTileOnlyConfig ++
  new chipyard.WithTileOnlyFireSimBridges
)
