package org.jetbrains.plugins.dotty.codeInspection.xml

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.dotty.codeInspection.xml.XmlExprIsNotSupportedInspection._
import org.jetbrains.plugins.scala.codeInspection.{AbstractFixOnPsiElement, AbstractInspection, InspectionBundle}
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypesEx
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScInterpolationPattern
import org.jetbrains.plugins.scala.lang.psi.api.expr.xml.ScXmlPattern
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory.{createInterpolatedStringInjection, createPatternFromText}
import org.jetbrains.plugins.scala.lang.psi.impl.expr.xml.ScXmlPatternImpl

/**
  * @author niksaz
  */
class XmlPatternsNotSupportedInspection extends AbstractInspection(id, name) {
  override def actionFor(holder: ProblemsHolder): PartialFunction[PsiElement, Any] = {
    case pattern: ScXmlPatternImpl =>
      holder.registerProblem(pattern, message, ERROR, new ReplaceXmlPatternQuickFix(pattern))
  }
}

class ReplaceXmlPatternQuickFix(token: PsiElement) extends AbstractFixOnPsiElement(name, token) {
  def replaceInjections(element: ScalaPsiElement): Unit = {
    implicit val manager = element.getManager
    element
      .findChildrenByType(ScalaTokenTypesEx.SCALA_IN_XML_INJECTION_START)
      .foreach {
        p => p.getParent.addBefore(createInterpolatedStringInjection, p)
      }

    for (child <- element.getChildren) {
      child match {
        case xmlExpr: ScXmlPattern =>
          val replaced = replaceXmlPattern(xmlExpr)
          xmlExpr.replace(replaced)
        case psiElem: ScalaPsiElement =>
          replaceInjections(psiElem)
        case _ =>
      }
    }
  }

  def replaceXmlPattern(xmlPattern: ScXmlPattern): ScInterpolationPattern = {
    replaceInjections(xmlPattern)
    implicit val manager = xmlPattern.getManager
    createPatternFromText("xml\"\"\"" + xmlPattern.getText + "\"\"\"").asInstanceOf[ScInterpolationPattern]
  }

  override def doApplyFix(project: Project): Unit = getElement match {
    case pattern: ScXmlPatternImpl if pattern.isValid =>
      implicit val manager = pattern.getManager
      val copy = createPatternFromText(pattern.getText).asInstanceOf[ScXmlPattern]
      pattern.replace(replaceXmlPattern(copy))
    case _ =>
  }
}

object XmlPatternsNotSupportedInspection {
  private[xml] val id = "XmlPatternsAreNotSupported"
  private[xml] val name = InspectionBundle.message("replace.with.interpolated.string")
    private[xml] val message = "Xml patterns are not supported in Dotty"
}