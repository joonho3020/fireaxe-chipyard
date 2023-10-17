package chipyard.config

import org.chipsalliance.cde.config.{Config, Field, Parameters}
import tracegen.{TraceGenSystem}
import chipyard.{BuildSystem}
import chipyard.clocking.{HasChipyardPRCI}
import testchipip.{GenericTracePortKey, GenericTracePortParams}

class TraceGenTop(implicit p: Parameters) extends TraceGenSystem
  with HasChipyardPRCI

class WithTracegenSystem extends Config((site, here, up) => {
  case BuildSystem => (p: Parameters) => new TraceGenTop()(p)
})

class WithGenericTraceIO extends Config((site, here, up) => {
  case GenericTracePortKey => Some(GenericTracePortParams())
})
