package chipyard

import freechips.rocketchip.config.{Config}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing}
import freechips.rocketchip.subsystem._

class WithExtMemIdBits(n: Int) extends Config((site, here, up) => {
  case ExtMem => up(ExtMem, site).map(x => x.copy(master = x.master.copy(idBits = n)))
})

class CompressAccBaseConfig extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=2048) ++
  new freechips.rocketchip.subsystem.WithNBanks(8) ++
  new WithExtMemIdBits(7) ++
  new freechips.rocketchip.subsystem.WithNMemoryChannels(4) ++
  new Config ((site, here, up) => {
    case SystemBusKey => up(SystemBusKey, site).copy(beatBytes = 32)
  }) ++
  new RocketConfig)

class CompressAccBaseConfig16MBL2 extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=16*1024) ++
  new CompressAccBaseConfig)

class CompressAccBaseConfig16MBL2And8MemChan extends Config(
  new freechips.rocketchip.subsystem.WithNMemoryChannels(8) ++
  new CompressAccBaseConfig16MBL2)

class SnappyBothConfig16MBL2And8MemChanRoCC extends Config(
  new compressacc.AcceleratorPlacementRoCC ++
  new compressacc.WithSnappyCompleteFireSim ++
  new CompressAccBaseConfig16MBL2And8MemChan)

class SnappyBothConfig16MBL2And8MemChanChiplet extends Config(
  new compressacc.AcceleratorPlacementChiplet ++
  new compressacc.WithSnappyCompleteFireSim ++
  new CompressAccBaseConfig16MBL2And8MemChan)

class SnappyBothConfig16MBL2And8MemChanPCIeLocalCache extends Config(
  new compressacc.AcceleratorPlacementPCIeLocalCache ++
  new compressacc.WithSnappyCompleteFireSim ++
  new CompressAccBaseConfig16MBL2And8MemChan)

class SnappyBothConfig16MBL2And8MemChanPCIeNoCache extends Config(
  new compressacc.AcceleratorPlacementPCIeNoCache ++
  new compressacc.WithSnappyCompleteFireSim ++
  new CompressAccBaseConfig16MBL2And8MemChan)

class SnappyDecompressorConfig16MBL2And8MemChanPCIeNoCache extends Config(
  new compressacc.AcceleratorPlacementPCIeNoCache ++
  new compressacc.WithSnappyDecompressor ++
  new CompressAccBaseConfig16MBL2And8MemChan)

class ZstdDecompressorConfig16MBL2And8MemChanSpec16 extends Config(
  new compressacc.WithZstdDecompressor16 ++
  new CompressAccBaseConfig16MBL2And8MemChan)

class ZstdDecompressorConfig16MBL2And8MemChanSpec32 extends Config(
  new compressacc.WithZstdDecompressor32 ++
  new CompressAccBaseConfig16MBL2And8MemChan)

class MergedCompressorLatencyInjectionHyperscaleRocketConfig8MBL2 extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=8*1024) ++
  new compressacc.WithMergedCompressorLatencyInjection ++
  new CompressAccBaseConfig)
