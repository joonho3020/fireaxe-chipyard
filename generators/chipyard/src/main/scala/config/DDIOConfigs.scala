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
