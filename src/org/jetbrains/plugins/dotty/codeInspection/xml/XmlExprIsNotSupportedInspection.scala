package org.jetbrains.plugins.dotty.codeInspection.xml

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.dotty.codeInspection.xml.XmlExprIsNotSupportedInspection._
import org.jetbrains.plugins.scala.codeInspection.{AbstractFixOnPsiElement, AbstractInspection, InspectionBundle}
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypesEx
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
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
  def replaceXmlExpr(psiElem: ScalaPsiElement): Unit = {
    for (child <- psiElem.getChildren) {
      child match {
        case scPsi: ScalaPsiElement =>
          replaceXmlExpr(scPsi)
        case _ =>
      }
    }
    psiElem match {
      case _: ScXmlExprImpl =>
        val anchorNode = psiElem.getNode.getFirstChildNode
        val xmlPrefix = ScalaPsiElementFactory.createInterpolatedStringPrefix("xml")(psiElem.getManager)
        val startQuotes = ScalaPsiElementFactory.createMultilineInterpolatedStringEnd(psiElem.getManager)
        psiElem.getNode.addChild(startQuotes, anchorNode)
        psiElem.getNode.addChild(xmlPrefix.getNode, startQuotes)
        val endQuotes = ScalaPsiElementFactory.createMultilineInterpolatedStringEnd(psiElem.getManager)
        psiElem.getNode.addChild(endQuotes)
      case _ =>
        val injectoionNode = psiElem.getNode.findChildByType(ScalaTokenTypesEx.SCALA_IN_XML_INJECTION_START)
        if (injectoionNode != null) {
          psiElem.getNode.addChild(
            ScalaPsiElementFactory.createInterpolatedStringInjection(psiElem.getManager), injectoionNode)
        }
    }
  }

  override def doApplyFix(project: Project): Unit = getElement match {
    case expr: ScXmlExprImpl if expr.isValid =>
      replaceXmlExpr(expr)
    case _ =>
  }
}

object XmlExprIsNotSupportedInspection {
  private[xml] val id = "XmpExprIsNotSupported"
  private[xml] val name = InspectionBundle.message("replace.with.interpolated.string")
  private[xml] val message = "Xml expressions are not supported in Dotty"
}
