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
  new chipyard.harness.WithLoopbackNIC ++                 // drive NIC IOs with loopback
  new icenet.WithIceNIC ++                                // add an IceNIC
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=2, capacityKB=512) ++
  new freechips.rocketchip.subsystem.WithNBanks(2) ++     // 2 L2 banks
  new freechips.rocketchip.subsystem.WithNBigCores(8) ++  // dual rocket-core
  new chipyard.config.AbstractConfig)
