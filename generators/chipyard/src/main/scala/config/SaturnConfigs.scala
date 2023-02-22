package chipyard

import freechips.rocketchip.config.{Config}

class SaturnConfig extends Config(
  new saturn.common.WithNSaturnCores(1) ++
  new chipyard.config.AbstractConfig
)

class SaturnCosimConfig extends Config(
  new chipyard.harness.WithCospike ++                            // attach spike-cosim
  new chipyard.config.WithTraceIO ++                             // enable the traceio
  new saturn.common.WithNSaturnCores(1) ++
  new chipyard.config.AbstractConfig
)
