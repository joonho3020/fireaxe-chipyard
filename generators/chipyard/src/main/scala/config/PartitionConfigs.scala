package chipyard

import org.chipsalliance.cde.config._
import freechips.rocketchip.subsystem.{ExtMem}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tile._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem.{SBUS, MBUS}

import constellation.channel._
import constellation.routing._
import constellation.topology._
import constellation.noc._
import constellation.soc.{GlobalNoCParams}

import scala.collection.immutable.ListMap

import compressacc._
import protoacc._


class SBUS16MempressRocketConfig extends Config(
  new mempress.WithMemPress(singleL2 = false, maxStreams = 4) ++                                    // use Mempress (memory traffic generation) accelerator
  new chipyard.config.WithSystemBusWidth(128) ++
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++
  new chipyard.config.AbstractConfig)

class Sha3SlowMemRocketConfig extends Config(
  new sha3.WithSha3Accel(fastMem = false) ++                                // add SHA3 rocc accelerator
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++
  new chipyard.config.AbstractConfig)

class DualCoreRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(2) ++
  new chipyard.config.AbstractConfig)

class DualCoreBoomConfig extends Config(
  new boom.common.WithNLargeBooms(2) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class QuadSmallCoreBoomConfig extends Config(
  new boom.common.WithNSmallBooms(4) ++
  new chipyard.config.AbstractConfig)

class HexaSmallCoreBoomConfig extends Config(
  new boom.common.WithNSmallBooms(6) ++
  new chipyard.config.AbstractConfig)

class OctaSmallCoreBoomConfig extends Config(
  new boom.common.WithNSmallBooms(8) ++
  new chipyard.config.AbstractConfig)

class DecaSmallCoreBoomConfig extends Config(
  new boom.common.WithNSmallBooms(10) ++
  new chipyard.config.AbstractConfig)

class TwelveSmallCoreBoomConfig extends Config(
  new boom.common.WithNSmallBooms(12) ++
  new chipyard.config.AbstractConfig)

class FourteenSmallCoreBoomConfig extends Config(
  new boom.common.WithNSmallBooms(14) ++
  new chipyard.config.AbstractConfig)

class SixteenSmallCoreBoomConfig extends Config(
  new boom.common.WithNSmallBooms(16) ++
  new chipyard.config.AbstractConfig)

class QuadCoreBoomConfig extends Config(
  new boom.common.WithNLargeBooms(4) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class HexaCoreBoomConfig extends Config(
  new boom.common.WithNLargeBooms(6) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class OctaCoreBoomConfig extends Config(
  new boom.common.WithNLargeBooms(8) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class HexadecaCoreBoomConfig extends Config(
  new boom.common.WithNLargeBooms(16) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)


class ThirtyTwoCoreBoomConfig extends Config(
  new boom.common.WithNLargeBooms(32) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class SixtyFourCoreBoomConfig extends Config(
  new boom.common.WithNLargeBooms(64) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class DualSha3RocketConfig extends Config(
  new sha3.WithSha3Accel(fastMem = false) ++
  new freechips.rocketchip.subsystem.WithNBigCores(2) ++
  new chipyard.config.AbstractConfig)

class QuadCoreRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(4) ++
  new chipyard.config.AbstractConfig)

class OctaCoreRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(8) ++
  new chipyard.config.AbstractConfig)

class QuadSha3RocketConfig extends Config(
  new sha3.WithSha3Accel(fastMem = false) ++
  new freechips.rocketchip.subsystem.WithNBigCores(4) ++
  new chipyard.config.AbstractConfig)

class MegaBoom64MBVCacheAnd4MemChan extends Config(
  new freechips.rocketchip.subsystem.WithNMemoryChannels(4) ++
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=64 * 1024) ++
  new chipyard.MegaBoomConfig)


class DualMempressRocketConfig extends Config(
  new mempress.WithMemPress(singleL2 = false, maxStreams = 4) ++                                    // use Mempress (memory traffic generation) accelerator
  new chipyard.config.WithSystemBusWidth(128) ++
  new freechips.rocketchip.subsystem.WithNBigCores(2) ++
  new chipyard.config.AbstractConfig)




class WithExtMemIdBits(n: Int) extends Config((site, here, up) => {
    case ExtMem => up(ExtMem, site).map(x => x.copy(master = x.master.copy(idBits = n)))
})


class HyperscaleRocketBaseConfig extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=2048) ++
  new freechips.rocketchip.subsystem.WithNBanks(8) ++
  new WithExtMemIdBits(7) ++
  new freechips.rocketchip.subsystem.WithNMemoryChannels(4) ++
  new chipyard.config.WithSystemBusWidth(32*8) ++
  new RocketConfig)


class HyperscaleZstdDecompressor32Config extends Config(
  new compressacc.WithZstdDecompressor32 ++
  new HyperscaleRocketBaseConfig)





class DualRocket4MemChanConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(8) ++
  new freechips.rocketchip.subsystem.WithNBanks(4) ++
  new freechips.rocketchip.subsystem.WithNMemoryChannels(4) ++
  new chipyard.config.AbstractConfig
)

class DualRocket4MemChanNoCConfig extends Config(
  new constellation.soc.WithCbusNoC(constellation.protocol.TLNoCParams(
    constellation.protocol.DiplomaticNetworkNodeMapping(
      inNodeMapping = ListMap(
        "serial-tl" -> 0),
      outNodeMapping = ListMap(
        "error" -> 1, "l2[0]" -> 2, "pbus" -> 3, "plic" -> 4,
        "clint" -> 5, "dmInner" -> 6, "bootrom" -> 7, "clock" -> 8, "reset_setter" -> 9)),
    NoCParams(
      topology = TerminalRouter(BidirectionalLine(10)),
      channelParamGen = (a, b) => UserChannelParams(Seq.fill(5) { UserVirtualChannelParams(4) }),
      routingRelation = NonblockingVirtualSubnetworksRouting(TerminalRouterRouting(BidirectionalLineRouting()), 5, 1))
  )) ++
  new constellation.soc.WithMbusNoC(constellation.protocol.TLNoCParams(
    constellation.protocol.DiplomaticNetworkNodeMapping(
      inNodeMapping = ListMap(
        "L2 InclusiveCache[0]" -> 1, "L2 InclusiveCache[1]" -> 2,
        "L2 InclusiveCache[2]" -> 5, "L2 InclusiveCache[3]" -> 6),
      outNodeMapping = ListMap(
        "system[0]" -> 0, "system[1]" -> 3,  "system[2]" -> 4 , "system[3]" -> 7,
        "serdesser" -> 0)),
    NoCParams(
      topology        = TerminalRouter(BidirectionalTorus1D(8)),
      channelParamGen = (a, b) => UserChannelParams(Seq.fill(10) { UserVirtualChannelParams(4) }),
      routingRelation = BlockingVirtualSubnetworksRouting(TerminalRouterRouting(BidirectionalTorus1DShortestRouting()), 5, 2))
  )) ++
  new constellation.soc.WithSbusNoC(constellation.protocol.TLNoCParams(
    constellation.protocol.DiplomaticNetworkNodeMapping(
      inNodeMapping = ListMap(
        "Core 0" -> 1, "Core 1" -> 2,
        "serial-tl" -> 0),
      outNodeMapping = ListMap(
        "system[0]" -> 4, "system[1]" -> 5, "system[2]" -> 6, "system[3]" -> 7,
        "pbus" -> 3)),
    NoCParams(
      topology        = TerminalRouter(Mesh2D(4, 2)),
      channelParamGen = (a, b) => UserChannelParams(Seq.fill(8) { UserVirtualChannelParams(4) }),
      routingRelation = BlockingVirtualSubnetworksRouting(TerminalRouterRouting(Mesh2DEscapeRouting()), 5, 1))
  )) ++
  new chipyard.DualRocket4MemChanConfig
)




class QuadRocketSbusRingNoCConfig extends Config(
  new constellation.soc.WithSbusNoC(constellation.protocol.TLNoCParams(
    constellation.protocol.DiplomaticNetworkNodeMapping(
      inNodeMapping = ListMap(
        "Core 0" -> 0,
        "Core 1" -> 1,
        "Core 2" -> 2,
        "Core 3" -> 3,
        "serial-tl" -> 4),
      outNodeMapping = ListMap(
        "system[0]" -> 5,
        "system[1]" -> 6,
        "system[2]" -> 7,
        "system[3]" -> 8,
        "pbus" -> 4)), // TSI is on the pbus, so serial-tl and pbus should be on the same node
    NoCParams(
      topology        = UnidirectionalTorus1D(9),
      channelParamGen = (a, b) => UserChannelParams(Seq.fill(10) { UserVirtualChannelParams(4) }),
      routingRelation = NonblockingVirtualSubnetworksRouting(UnidirectionalTorus1DDatelineRouting(), 5, 2))
  )) ++
  new freechips.rocketchip.subsystem.WithNBigCores(4) ++
  new freechips.rocketchip.subsystem.WithNBanks(4) ++
  new chipyard.config.AbstractConfig
)


class WithHyperscaleAccels extends Config ((site, here, up) => {
  case ProtoTLB => Some(TLBConfig(nSets = 4, nWays = 4, nSectors = 1, nSuperpageEntries = 1))
  case CompressAccelTLB => Some(TLBConfig(nSets = 4, nWays = 4, nSectors = 1, nSuperpageEntries = 1))
  case ZstdCompressorKey => Some(ZstdCompressorConfig(queDepth = 4))
  case HufCompressUnrollCnt => 4
  case HufCompressDicBuilderProcessedStatBytesPerCycle => 4
  case FSECompressDicBuilderProcessedStatBytesPerCycle => 4
  case ZstdLiteralLengthMaxAccuracy => 7
  case ZstdMatchLengthMaxAccuracy => 7
  case ZstdOffsetMaxAccuracy => 6
  case RemoveSnappyFromMergedAccelerator => true
  case CompressAccelPrintfEnable => true
  case ZstdDecompressorCmdQueDepth => 4
  case HufDecompressDecompAtOnce => 4
  case NoSnappy => true

  case BuildRoCC => Seq(
    (p: Parameters) => {
      val protoacc = LazyModule(new ProtoAccel(OpcodeSet.custom2)(p))
      protoacc
    },
    (p: Parameters) => {
      val protoaccser = LazyModule(new ProtoAccelSerializer(OpcodeSet.custom3)(p))
      protoaccser
    },
    (p: Parameters) => {
      val zstddecomp = LazyModule(new ZstdDecompressor(OpcodeSet.custom0)(p))
      zstddecomp
    },
    (p: Parameters) => {
      val zstdcomp = LazyModule(new ZstdCompressor(OpcodeSet.custom1)(p))
      zstdcomp
    }
  )
})

class HyperscaleRocketAccelsConfig extends Config(
  new WithHyperscaleAccels ++
  new HyperscaleRocketBaseConfig
  )

class WithZstdComp extends Config ((site, here, up) => {
  case CompressAccelTLB => Some(TLBConfig(nSets = 4, nWays = 4, nSectors = 1, nSuperpageEntries = 1))
  case ZstdCompressorKey => Some(ZstdCompressorConfig(queDepth = 4))
  case HufCompressUnrollCnt => 4
  case HufCompressDicBuilderProcessedStatBytesPerCycle => 4
  case FSECompressDicBuilderProcessedStatBytesPerCycle => 4
  case ZstdLiteralLengthMaxAccuracy => 7
  case ZstdMatchLengthMaxAccuracy => 7
  case ZstdOffsetMaxAccuracy => 6
  case RemoveSnappyFromMergedAccelerator => true
  case CompressAccelPrintfEnable => true

  case BuildRoCC => Seq(
    (p: Parameters) => {
      val zstdcomp = LazyModule(new ZstdCompressor(OpcodeSet.custom1)(p))
      zstdcomp
    }
  )
})

class HyperscaleZstdCompressorRocketConfig extends Config(
  new WithZstdComp ++
  new HyperscaleRocketBaseConfig
  )
