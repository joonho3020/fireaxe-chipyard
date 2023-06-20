package chipyard

import org.chipsalliance.cde.config.{Config}

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

class DualSha3RocketConfig extends Config(
  new sha3.WithSha3Accel(fastMem = false) ++
  new freechips.rocketchip.subsystem.WithNBigCores(2) ++
  new chipyard.config.AbstractConfig)

class QuadCoreRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(4) ++
  new chipyard.config.AbstractConfig)

class QuadSha3RocketConfig extends Config(
  new sha3.WithSha3Accel(fastMem = false) ++
  new freechips.rocketchip.subsystem.WithNBigCores(4) ++
  new chipyard.config.AbstractConfig)

class MegaBoom128MBVCacheAnd4MemChan extends Config(
  new freechips.rocketchip.subsystem.WithNMemoryChannels(4) ++
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=32, capacityKB=131072) ++
  new chipyard.MegaBoomConfig)
