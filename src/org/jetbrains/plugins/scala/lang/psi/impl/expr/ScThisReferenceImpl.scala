package org.jetbrains.plugins.scala
package lang
package psi
package impl
package expr

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.scala.lang.psi.api.ScalaElementVisitor
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.templates.ScTemplateBody
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScTemplateDefinition, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.types._
import org.jetbrains.plugins.scala.lang.psi.types.api.TypeSystem
import org.jetbrains.plugins.scala.lang.psi.types.api.designator.{DesignatorOwner, ScThisType}
import org.jetbrains.plugins.scala.lang.psi.types.result.{Failure, Success, TypeResult, TypingContext}

/**
 * @author Alexander Podkhalyuzin
 * Date: 06.03.2008
 */

class ScThisReferenceImpl(node: ASTNode) extends ScalaPsiElementImpl(node) with ScThisReference {
  override def toString: String = "ThisReference"

  protected override def innerType(ctx: TypingContext): TypeResult[ScType] = refTemplate match {
    case Some(td) if td.isMetaAnnotatationImpl => TypeResult.fromOption(ScalaPsiElementFactory.createTypeFromText("scala.meta.Stat", this, null))
    case Some(td) => ScThisReferenceImpl.getThisTypeForTypeDefinition(td, this)
    case _ => Failure("Cannot infer type", Some(this))
  }

  def refTemplate: Option[ScTemplateDefinition] = reference match {
    case Some(ref) => ref.resolve() match {
      case td: ScTypeDefinition if PsiTreeUtil.isContextAncestor(td, ref, false) => Some(td)
      case _ => None
    }
    case None =>
      val encl = PsiTreeUtil.getContextOfType(this, false, classOf[ScTemplateBody])
      if (encl != null) Some(PsiTreeUtil.getContextOfType(encl, false, classOf[ScTemplateDefinition])) else None
  }

  override def accept(visitor: ScalaElementVisitor) {
    visitor.visitThisReference(this)
  }

  override def accept(visitor: PsiElementVisitor) {
    visitor match {
      case visitor: ScalaElementVisitor => visitor.visitThisReference(this)
      case _ => super.accept(visitor)
    }
  }
}

object ScThisReferenceImpl {
  def getThisTypeForTypeDefinition(td: ScTemplateDefinition, expr: ScExpression)
                                  (implicit typeSystem: TypeSystem): TypeResult[ScType] = {
    // SLS 6.5:  If the expression’s expected type is a stable type,
    // or C .this occurs as the prefix of a selection, its type is C.this.type,
    // otherwise it is the self type of class C .
    val element = Some(expr)
    val result = expr.getContext match {
      case referenceExpression: ScReferenceExpression if referenceExpression.qualifier.contains(expr) =>
        ScThisType(td)
      case _ => expr.expectedType() match {
        case Some(designatorOwner: DesignatorOwner) if designatorOwner.isStable =>
          ScThisType(td)
        case _ =>
          td.getTypeWithProjections(TypingContext.empty, thisProjections = true).map {
            case scType => td.selfType.map(scType.glb(_)).getOrElse(scType)
          } match {
            case Success(scType, _) => scType
            case _ => return Failure("No clazz type found", Some(expr))
          }
      }
    }
    Success(result, element)
  }
}