package org.jetbrains.plugins.dotty.codeInspection.xml

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.dotty.codeInspection.xml.XmlExprIsNotSupportedInspection._
import org.jetbrains.plugins.scala.codeInspection.{AbstractFixOnPsiElement, AbstractInspection, InspectionBundle}
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypesEx
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.base.ScInterpolatedStringLiteral
import org.jetbrains.plugins.scala.lang.psi.api.expr.xml.ScXmlExpr
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory._
import org.jetbrains.plugins.scala.lang.psi.impl.expr.xml._

/**
  * @author niksaz
  */
class XmlExprIsNotSupportedInspection extends AbstractInspection(id, name) {
  override def actionFor(holder: ProblemsHolder): PartialFunction[PsiElement, Any] = {
    case expr: ScXmlExprImpl =>
      holder.registerProblem(expr, message, ERROR, new ReplaceXmlExprQuickFix(expr))
  }
}

class ReplaceXmlExprQuickFix(token: PsiElement) extends AbstractFixOnPsiElement(name, token) {
  def replaceInjections(element: ScalaPsiElement): Unit = {
    implicit val manager = element.getManager
    element
      .findChildrenByType(ScalaTokenTypesEx.SCALA_IN_XML_INJECTION_START)
      .foreach {
        p => p.getParent.addBefore(createInterpolatedStringInjection, p)
      }

    for (child <- element.getChildren) {
      child match {
        case xmlExpr: ScXmlExpr =>
          val replaced = replaceXmlExpr(xmlExpr)
          xmlExpr.replace(replaced)
        case psiElem: ScalaPsiElement =>
          replaceInjections(psiElem)
        case _ =>
      }
    }
  }

  def replaceXmlExpr(xmlExpr: ScXmlExpr): ScInterpolatedStringLiteral = {
    replaceInjections(xmlExpr)
    implicit val manager = xmlExpr.getManager
    createExpressionFromText("xml\"\"\"" + xmlExpr.getText + "\"\"\"").asInstanceOf[ScInterpolatedStringLiteral]
  }

  override def doApplyFix(project: Project): Unit = getElement match {
    case expr: ScXmlExpr if expr.isValid =>
      implicit val manager = expr.getManager
      val copy = createExpressionFromText(expr.getText).asInstanceOf[ScXmlExpr]
      expr.replace(replaceXmlExpr(copy))
    case _ =>
  }
}

object XmlExprIsNotSupportedInspection {
  private[xml] val id = "XmpExprIsNotSupported"
  private[xml] val name = InspectionBundle.message("replace.with.interpolated.string")
  private[xml] val message = "Xml expressions are not supported in Dotty"
}
