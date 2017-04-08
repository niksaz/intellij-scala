package org.jetbrains.plugins.dotty.codeInspection.lazyVal

import com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.codeInspection.{AbstractFixOnPsiElement, AbstractInspection, InspectionBundle}
import org.jetbrains.plugins.dotty.codeInspection.lazyVal.LazyValNotVolatileInspection._
import org.jetbrains.plugins.scala.lang.psi.impl.statements.ScPatternDefinitionImpl
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.kLAZY
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory

/**
  * @author niksaz
  */
class LazyValNotVolatileInspection extends AbstractInspection(id, name) {
  override def actionFor(holder: ProblemsHolder): PartialFunction[PsiElement, Any] = {
    case patternDef: ScPatternDefinitionImpl if isApplicableToDef(patternDef) =>
      val lazyElem = patternDef.getModifierList.findFirstChildByType(kLAZY)
      holder.registerProblem(lazyElem, message, GENERIC_ERROR_OR_WARNING, new LazyValNotVolatileQuickFix(patternDef))
  }
}

class LazyValNotVolatileQuickFix(token: PsiElement) extends AbstractFixOnPsiElement(name, token) {
  override def doApplyFix(project: Project): Unit = getElement match {
    case patternDef: ScPatternDefinitionImpl if patternDef.isValid =>
      val modifiers = patternDef.getModifierList
      val lazyElem = modifiers.findFirstChildByType(kLAZY)
      val volatile = ScalaPsiElementFactory.createAnAnnotation("volatile")(patternDef.getManager)
      modifiers.addBefore(volatile, lazyElem)
    case _ =>
  }
}

object LazyValNotVolatileInspection {
  private[lazyVal] val id = "LazyValNotVolatile"
  private[lazyVal] val name = InspectionBundle.message("add.volatile.annotation")
  private[lazyVal] val message = "lazy val should be annotated with @volatile"

  def isApplicableToDef(patternDef: ScPatternDefinitionImpl): Boolean = {
    patternDef.getModifierList.findFirstChildByType(kLAZY) != null && !patternDef.hasAnnotation("scala.volatile")
  }
}