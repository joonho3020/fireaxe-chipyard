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
