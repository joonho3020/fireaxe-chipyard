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

class OneBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++
  new chipyard.config.AbstractConfig)

class TwoBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(2) ++
  new chipyard.config.AbstractConfig)

class ThreeBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(3) ++
  new chipyard.config.AbstractConfig)

class FourBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(4) ++
  new chipyard.config.AbstractConfig)

class FiveBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(5) ++
  new chipyard.config.AbstractConfig)

class SixBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(6) ++
  new chipyard.config.AbstractConfig)

class SevenBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(7) ++
  new chipyard.config.AbstractConfig)

class EightBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(8) ++
  new chipyard.config.AbstractConfig)

class TwelveBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(12) ++
  new chipyard.config.AbstractConfig)

class SixTeenBigRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(16) ++
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
