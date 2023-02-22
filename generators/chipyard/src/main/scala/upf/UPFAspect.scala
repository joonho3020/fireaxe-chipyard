// See LICENSE for license details
package chipyard.upf

import chisel3.experimental.{BaseModule}
import chisel3.aop.Aspect
import firrtl.{AnnotationSeq}
import chipyard.TestHarness

abstract class UPFAspect[T <: TestHarness](upf: UPFFunc.UPFFunction) extends Aspect[T] {

  final override def toAnnotation(top: T): AnnotationSeq = {
    upf(top.dut)
    AnnotationSeq(Seq()) // noop
  }
  
}

object UPFFunc {
  type UPFFunction = PartialFunction[BaseModule, Unit]
}