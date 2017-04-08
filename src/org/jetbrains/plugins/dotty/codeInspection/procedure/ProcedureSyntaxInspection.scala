package org.jetbrains.plugins.dotty.codeInspection.procedure

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.dotty.codeInspection.procedure.ProcedureSyntaxInspection._
import org.jetbrains.plugins.scala.codeInspection.methodSignature.quickfix.InsertReturnTypeAndEquals
import org.jetbrains.plugins.scala.codeInspection.{AbstractFixOnPsiElement, AbstractInspection, InspectionBundle}
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.kDEF
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition

/**
  * @author niksaz
  */
class ProcedureSyntaxInspection extends AbstractInspection(id, name) {
  override def actionFor(holder: ProblemsHolder): PartialFunction[PsiElement, Any] = {
    case funcDef: ScFunctionDefinition if !funcDef.hasAssign =>
      funcDef.findChildrenByType(kDEF).foreach { token =>
        holder.registerProblem(token, message, ERROR, new ProcedureSyntaxQuickFix(token))
      }
  }
}

class ProcedureSyntaxQuickFix(token: PsiElement) extends AbstractFixOnPsiElement(name, token) {
  override def doApplyFix(project: Project): Unit = getElement match {
    case element if element.isValid =>
      val functionDefinition = element.getParent.asInstanceOf[ScFunctionDefinition]
      new InsertReturnTypeAndEquals(functionDefinition).doApplyFix(project)
    case _ =>
  }
}

object ProcedureSyntaxInspection {
  private[procedure] val id = "ProcedureSyntax"
  private[procedure] val name = InspectionBundle.message("add.unit.type.annotation")
  private[procedure] val message = "Procedure syntax is not supported in Dotty"
}