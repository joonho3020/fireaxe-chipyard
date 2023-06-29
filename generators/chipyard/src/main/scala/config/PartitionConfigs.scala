package chipyard

import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.subsystem.{ExtMem}

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

class QuadCoreBoomConfig extends Config(
  new boom.common.WithNLargeBooms(4) ++                          // large boom config
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
