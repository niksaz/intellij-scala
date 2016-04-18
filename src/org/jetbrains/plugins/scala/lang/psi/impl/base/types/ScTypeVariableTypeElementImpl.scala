package org.jetbrains.plugins.scala
package lang.psi.impl.base.types

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElementImpl
import org.jetbrains.plugins.scala.lang.psi.api.base.types.{ScTypeElementExt, ScTypeVariableTypeElement}
import org.jetbrains.plugins.scala.lang.psi.types.result.TypingContext
import org.jetbrains.plugins.scala.lang.psi.types.ScExistentialArgument
import org.jetbrains.plugins.scala.lang.psi.types.api.{Any, Nothing}

/**
 * @author Alefas
 * @since 26/09/14.
 */
class ScTypeVariableTypeElementImpl(node: ASTNode) extends ScalaPsiElementImpl(node) with ScTypeVariableTypeElement {
  override protected def innerType(ctx: TypingContext) = this.success(ScExistentialArgument(name, List.empty, Nothing, Any))

  override def nameId: PsiElement = findChildByType[PsiElement](ScalaTokenTypes.tIDENTIFIER)

  override def toString: String = s"$typeName: $name"
}
