package chipyard

import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing}




class OneRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++         // single rocket-core
  new chipyard.config.AbstractConfig)

class TwoRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(2) ++         // single rocket-core
  new chipyard.config.AbstractConfig)

class FourRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(4) ++         // single rocket-core
  new chipyard.config.AbstractConfig)

class EightRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(8) ++         // single rocket-core
  new chipyard.config.AbstractConfig)

class SixteenRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(16) ++         // single rocket-core
  new chipyard.config.AbstractConfig)
