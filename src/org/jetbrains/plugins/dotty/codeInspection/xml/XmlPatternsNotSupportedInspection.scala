package org.jetbrains.plugins.dotty.codeInspection.xml

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.dotty.codeInspection.xml.XmlPatternsNotSupportedInspection._
import org.jetbrains.plugins.scala.codeInspection.{AbstractFixOnPsiElement, AbstractInspection, InspectionBundle}
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypesEx
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
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
  def replaceXmlPattern(psiElem: ScalaPsiElement): Unit = {
    for (e <- psiElem.getChildren) {
      e match {
        case sc: ScalaPsiElement =>
          replaceXmlPattern(sc)
        case _ =>
      }
    }
    psiElem match {
      case _: ScXmlPatternImpl =>
        val anchorNode = psiElem.getNode.getFirstChildNode
        val xmlPrefix = ScalaPsiElementFactory.createInterpolatedStringPrefix("xml")(psiElem.getManager)
        val startQuotes = ScalaPsiElementFactory.createMultilineInterpolatedStringEnd(psiElem.getManager)
        psiElem.getNode.addChild(startQuotes, anchorNode)
        psiElem.getNode.addChild(xmlPrefix.getNode, startQuotes)
        val endQuotes = ScalaPsiElementFactory.createMultilineInterpolatedStringEnd(psiElem.getManager)
        psiElem.getNode.addChild(endQuotes)
      case _ =>
    }
    val injectoionNode = psiElem.getNode.findChildByType(ScalaTokenTypesEx.SCALA_IN_XML_INJECTION_START)
    if (injectoionNode != null) {
      psiElem.getNode.addChild(
        ScalaPsiElementFactory.createInterpolatedStringInjection(psiElem.getManager), injectoionNode)
    }
  }

  override def doApplyFix(project: Project): Unit = getElement match {
    case pattern: ScXmlPatternImpl =>
      replaceXmlPattern(pattern)
    case _ =>
  }
}

object XmlPatternsNotSupportedInspection {
  private[xml] val id = "XmlPatternsAreNotSupported"
  private[xml] val name = InspectionBundle.message("replace.with.interpolated.string")
    private[xml] val message = "Xml patterns are not supported in Dotty"
}