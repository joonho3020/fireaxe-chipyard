package chipyard

import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing}

// --------------
// Rocket Configs
// --------------

class DDIORocketConfig extends Config(
  new chipyard.harness.WithLoopbackNIC ++                  // drive NIC IOs with loopback
  new icenet.WithIceNIC ++                                 // add an IceNIC
  new freechips.rocketchip.subsystem.WithNBanks(2) ++      // 2 L2 banks
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++   // single rocket-core
  new chipyard.config.AbstractConfig)

class DDIODualRocketConfig extends Config(
  new chipyard.harness.WithLoopbackNIC ++                 // drive NIC IOs with loopback
  new icenet.WithIceNIC ++                                // add an IceNIC
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new freechips.rocketchip.subsystem.WithNBigCores(2) ++  // dual rocket-core
  new chipyard.config.AbstractConfig)

class DDIOOctaRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=512) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new freechips.rocketchip.subsystem.WithNBigCores(8) ++  // dual rocket-core
  new chipyard.config.AbstractConfig)

class DDIOOctaLoopbackNICRocketConfig extends Config(
  new chipyard.harness.WithLoopbackNIC ++                 // drive NIC IOs with loopback
  new icenet.WithIceNIC ++                                // add an IceNIC
  new chipyard.DDIOOctaRocketConfig)

class DDIOQuadLargeBoomConfig extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=512) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new boom.common.WithNLargeBooms(4) ++
  new chipyard.config.AbstractConfig)

class DDIOOctaLargeBoomConfig extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=512) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new boom.common.WithNLargeBooms(8) ++
  new chipyard.config.AbstractConfig)

class DDIOOctaLoopbackNICLargeBoomConfig extends Config(
  new chipyard.harness.WithLoopbackNIC ++                 // drive NIC IOs with loopback
  new icenet.WithIceNIC ++                                // add an IceNIC
  new chipyard.DDIOOctaLargeBoomConfig)


class DDIODoDecaRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=512) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new freechips.rocketchip.subsystem.WithNBigCores(12) ++  // dual rocket-core
  new chipyard.config.AbstractConfig)

class DDIODoDeca128kBRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=128) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new freechips.rocketchip.subsystem.WithNBigCores(12) ++  // dual rocket-core
  new chipyard.config.AbstractConfig)

class DDIODoDecaBoom128kBL2SbusRingNoCConfig extends Config(
  new constellation.soc.WithSbusNoC(constellation.protocol.TLNoCParams(
    constellation.protocol.DiplomaticNetworkNodeMapping(
      inNodeMapping = ListMap((0 until 12).map(idx => s"Core ${idx} " -> idx) :_*) + ("serial-tl" -> 12),
      outNodeMapping = ListMap(
        "system[0]" -> 13,
        "system[1]" -> 14,
        "system[2]" -> 15,
        "system[3]" -> 16,
        "pbus" -> 12)), // TSI is on the pbus, so serial-tl and pbus should be on the same node
    NoCParams(
      topology        = UnidirectionalTorus1D(17),
      channelParamGen = (a, b) => UserChannelParams(Seq.fill(10) { UserVirtualChannelParams(4) }),
      routingRelation = NonblockingVirtualSubnetworksRouting(UnidirectionalTorus1DDatelineRouting(), 5, 2))
  )) ++
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=128) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new boom.common.WithCloneBoomTiles(11, 0) ++
  new boom.common.WithNLargeBooms(1) ++
  new chipyard.config.AbstractConfig)

class DDIODoDecaBoom256kBL2SbusRingNoCConfig extends Config(
  new constellation.soc.WithSbusNoC(constellation.protocol.TLNoCParams(
    constellation.protocol.DiplomaticNetworkNodeMapping(
      inNodeMapping = ListMap((0 until 12).map(idx => s"Core ${idx} " -> idx) :_*) + ("serial-tl" -> 12),
      outNodeMapping = ListMap(
        "system[0]" -> 13,
        "system[1]" -> 14,
        "system[2]" -> 15,
        "system[3]" -> 16,
        "pbus" -> 12)), // TSI is on the pbus, so serial-tl and pbus should be on the same node
    NoCParams(
      topology        = UnidirectionalTorus1D(17),
      channelParamGen = (a, b) => UserChannelParams(Seq.fill(10) { UserVirtualChannelParams(4) }),
      routingRelation = NonblockingVirtualSubnetworksRouting(UnidirectionalTorus1DDatelineRouting(), 5, 2))
  )) ++
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=256) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new boom.common.WithCloneBoomTiles(11, 0) ++
  new boom.common.WithNLargeBooms(1) ++
  new chipyard.config.AbstractConfig)

class DDIODoDecaBoom512kBL2SbusRingNoCConfig extends Config(
  new constellation.soc.WithSbusNoC(constellation.protocol.TLNoCParams(
    constellation.protocol.DiplomaticNetworkNodeMapping(
      inNodeMapping = ListMap((0 until 12).map(idx => s"Core ${idx} " -> idx) :_*) + ("serial-tl" -> 12),
      outNodeMapping = ListMap(
        "system[0]" -> 13,
        "system[1]" -> 14,
        "system[2]" -> 15,
        "system[3]" -> 16,
        "pbus" -> 12)), // TSI is on the pbus, so serial-tl and pbus should be on the same node
    NoCParams(
      topology        = UnidirectionalTorus1D(17),
      channelParamGen = (a, b) => UserChannelParams(Seq.fill(10) { UserVirtualChannelParams(4) }),
      routingRelation = NonblockingVirtualSubnetworksRouting(UnidirectionalTorus1DDatelineRouting(), 5, 2))
  )) ++
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=512) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new boom.common.WithCloneBoomTiles(11, 0) ++
  new boom.common.WithNLargeBooms(1) ++
  new chipyard.config.AbstractConfig)
