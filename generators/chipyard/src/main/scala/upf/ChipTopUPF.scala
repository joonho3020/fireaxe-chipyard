// See LICENSE for license details
package chipyard.upf

import chipyard.{TestHarness, ChipTopLazyRawModuleImp, DigitalTop}
import freechips.rocketchip.diplomacy.LazyModule

import scala.collection.mutable.ListBuffer

import scalax.collection.mutable.Graph
import scalax.collection.GraphPredef._, scalax.collection.GraphEdge._

object ChipTopUPF {

  def default: UPFFunc.UPFFunction = {
    case top: ChipTopLazyRawModuleImp => {      
      val modulesList = getLazyModules(top.outer.lazySystem)
      val pdList = createPowerDomains(modulesList)
      val g = connectPDHierarchy(pdList)
      for (node <- g.nodes.filter(_.diPredecessors.isEmpty)) { // all nodes without parents
        g.outerNodeTraverser(node).foreach(UPFGenerator.generateUPF(_, g))
      }
    }
  }

  def getLazyModules(top: LazyModule): ListBuffer[LazyModule] = {
    var i = 0
    var result = new ListBuffer[LazyModule]()
    result.append(top)
    while (i < result.length) {
      val lazyMod = result(i)
      for (child <- lazyMod.getChildren) {
        result.append(child)
      }
      i += 1
    }
    return result
  }

  def createPowerDomains(modulesList: ListBuffer[LazyModule]): ListBuffer[PowerDomain] = {
    var pdList = ListBuffer[PowerDomain]()
    for (pdInput <- UPFInputs.upfInfo) {
      var pdModules = ListBuffer[LazyModule]()
      for (moduleName <- pdInput.moduleList) {
        val module = modulesList.filter(_.module.name == moduleName)
        if (module.length == 1) { // filter returns a collection
          pdModules.append(module(0))
        } else {
          throw new Exception(s"PowerDomainInput module list doesn't exist in design. Modules matching input ${moduleName} are: ${module}.")
        }
      }
      val pd = new PowerDomain(name=pdInput.name, modules=pdModules, 
                                isTop=pdInput.isTop, isGated=pdInput.isGated, 
                                highVoltage=pdInput.highVoltage, lowVoltage=pdInput.lowVoltage)
      pdList.append(pd)
    }
    return pdList
  }

  def connectPDHierarchy(pdList: ListBuffer[PowerDomain]): Graph[PowerDomain, DiEdge] = {
    var g = Graph[PowerDomain, DiEdge]()
    for (pd <- pdList) {
      val pdInput = UPFInputs.upfInfo.filter(_.name == pd.name)(0)
      val childPDs = pdList.filter(x => pdInput.childrenPDs.contains(x.name))
      for (childPD <- childPDs) {
        g += (pd ~> childPD) // directed edge from pd to childPD
      }
    }
    return g
  }

}

case object ChipTopUPFAspect extends UPFAspect[chipyard.TestHarness](ChipTopUPF.default)