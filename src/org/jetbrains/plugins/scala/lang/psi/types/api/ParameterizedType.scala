package org.jetbrains.plugins.scala.lang.psi.types.api

import java.util.concurrent.ConcurrentMap

import com.intellij.util.containers.ContainerUtil
import org.jetbrains.plugins.scala.extensions.TraversableExt
import org.jetbrains.plugins.scala.lang.psi.types.api.ParameterizedType.substitutorCache
import org.jetbrains.plugins.scala.lang.psi.types.{ScSubstitutor, ScType}

/**
  * @author adkozlov
  */
trait ParameterizedType extends TypeInTypeSystem with ValueType {
  val designator: ScType
  val typeArguments: Seq[ScType]

  def substitutor: ScSubstitutor = Option(substitutorCache.get(this)).getOrElse {
    val result = substitutorInner
    substitutorCache.put(this, result)
    result
  }

  protected def substitutorInner: ScSubstitutor

  override def removeAbstracts = ParameterizedType(designator.removeAbstracts,
    typeArguments.map(_.removeAbstracts))

  override def updateSubtypes(update: (ScType) => (Boolean, ScType), visited: Set[ScType]): ValueType = {
    ParameterizedType(
      designator.recursiveUpdate(update, visited),
      typeArguments.map(_.recursiveUpdate(update, visited))
    )
  }

  override def typeDepth: Int = {
    val result = designator.typeDepth
    typeArguments.map(_.typeDepth) match {
      case Seq() => result //todo: shouldn't be possible
      case seq => result.max(seq.max + 1)
    }
  }

  override def isFinalType: Boolean = designator.isFinalType && typeArguments.filterBy(classOf[TypeParameterType])
    .forall(_.isInvariant)
}

object ParameterizedType {
  val substitutorCache: ConcurrentMap[ParameterizedType, ScSubstitutor] =
    ContainerUtil.createConcurrentWeakMap[ParameterizedType, ScSubstitutor]()

  def apply(designator: ScType, typeArguments: Seq[ScType])
           (implicit typeSystem: TypeSystem): ValueType = typeSystem.parameterizedType(designator, typeArguments)

  def unapply(parameterized: ParameterizedType): Option[(ScType, Seq[ScType])] =
    Some(parameterized.designator, parameterized.typeArguments)
}
