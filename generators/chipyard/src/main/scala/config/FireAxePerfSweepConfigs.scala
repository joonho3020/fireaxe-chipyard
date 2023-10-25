package chipyard

import org.chipsalliance.cde.config._
import freechips.rocketchip.subsystem.{ExtMem}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tile._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem.{SBUS, MBUS}

import constellation.channel._
import constellation.routing._
import constellation.topology._
import constellation.noc._
import constellation.soc.{GlobalNoCParams}

import scala.collection.immutable.ListMap

import compressacc._
import protoacc._



//////////////////////////////////////////////////////////////////////////////

class OneSmallRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(1) ++
  new chipyard.config.AbstractConfig)

class TwoSmallRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(2) ++
  new chipyard.config.AbstractConfig)

class ThreeSmallRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(3) ++
  new chipyard.config.AbstractConfig)

class FourSmallRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(4) ++
  new chipyard.config.AbstractConfig)

class FiveSmallRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(5) ++
  new chipyard.config.AbstractConfig)

class SixSmallRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(6) ++
  new chipyard.config.AbstractConfig)

class SevenSmallRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(7) ++
  new chipyard.config.AbstractConfig)

class EightSmallRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(8) ++
  new chipyard.config.AbstractConfig)

//////////////////////////////////////////////////////////////////////////////

class OneLargeBoomConfig extends Config(
  new boom.common.WithNLargeBooms(1) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class TwoLargeBoomConfig extends Config(
  new boom.common.WithNLargeBooms(2) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class ThreeLargeBoomConfig extends Config(
  new boom.common.WithNLargeBooms(3) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class FourLargeBoomConfig extends Config(
  new boom.common.WithNLargeBooms(4) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class FiveLargeBoomConfig extends Config(
  new boom.common.WithNLargeBooms(5) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)

class SixLargeBoomConfig extends Config(
  new boom.common.WithNLargeBooms(6) ++                          // large boom config
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.AbstractConfig)
