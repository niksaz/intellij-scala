package org.jetbrains.plugins.scala
package lang
package psi
package types

import org.jetbrains.plugins.scala.lang.psi.types.api._
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.NonValueType



/**
 * This type works like undefined type, but you cannot use this type
 * to resolve generics. It's important if two local type
 * inferences work together.
 */
case class ScAbstractType(parameterType: TypeParameterType, lower: ScType, upper: ScType) extends ScalaType with NonValueType {
  private var hash: Int = -1

  override def hashCode: Int = {
    if (hash == -1) {
      hash = (upper.hashCode() * 31 + lower.hashCode()) * 31 + parameterType.arguments.hashCode()
    }
    hash
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case ScAbstractType(oTpt, oLower, oUpper) =>
        lower.equals(oLower) && upper.equals(oUpper) && parameterType.arguments.equals(oTpt.arguments)
      case _ => false
    }
  }

  override def equivInner(r: ScType, uSubst: ScUndefinedSubstitutor, falseUndef: Boolean)
                         (implicit typeSystem: TypeSystem): (Boolean, ScUndefinedSubstitutor) = {
    r match {
      case _ if falseUndef => (false, uSubst)
      case _ =>
        var t: (Boolean, ScUndefinedSubstitutor) = r.conforms(upper, uSubst)
        if (!t._1) return (false, uSubst)
        t = lower.conforms(r, t._2)
        if (!t._1) return (false, uSubst)
        (true, t._2)
    }
  }

  def inferValueType: TypeParameterType = parameterType

  def simplifyType: ScType = {
    if (upper.equiv(Any)) lower else if (lower.equiv(Nothing)) upper else lower
  }

  override def removeAbstracts: ScType = simplifyType

  override def updateSubtypes(update: ScType => (Boolean, ScType), visited: Set[ScType]): ScAbstractType = {
    try {
      ScAbstractType(
        parameterType.recursiveUpdate(update, visited).asInstanceOf[TypeParameterType],
        lower.recursiveUpdate(update, visited),
        upper.recursiveUpdate(update, visited)
      )
    }
    catch {
      case _: ClassCastException => throw new RecursiveUpdateException
    }
  }

  override def recursiveVarianceUpdateModifiable[T](data: T, update: (ScType, Int, T) => (Boolean, ScType, T),
                                           variance: Int = 1): ScType = {
    update(this, variance, data) match {
      case (true, res, _) => res
      case (_, _, newData) =>
        try {
          ScAbstractType(parameterType.recursiveVarianceUpdateModifiable(newData, update, variance).asInstanceOf[TypeParameterType],
            lower.recursiveVarianceUpdateModifiable(newData, update, -variance),
            upper.recursiveVarianceUpdateModifiable(newData, update, variance))
        }
        catch {
          case _: ClassCastException => throw new RecursiveUpdateException
        }
    }
  }

  override def visitType(visitor: TypeVisitor): Unit = visitor match {
    case scalaVisitor: ScalaTypeVisitor => scalaVisitor.visitAbstractType(this)
    case _ =>
  }
}
