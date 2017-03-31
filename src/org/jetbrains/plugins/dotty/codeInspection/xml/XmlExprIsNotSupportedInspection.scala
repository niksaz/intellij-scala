package org.jetbrains.plugins.dotty.codeInspection.xml

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.dotty.codeInspection.xml.XmlExprIsNotSupportedInspection._
import org.jetbrains.plugins.scala.codeInspection.{AbstractFixOnPsiElement, AbstractInspection, InspectionBundle}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.impl.expr.xml._
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory.createElement

/**
  * @author niksaz
  */
class XmlExprIsNotSupportedInspection extends AbstractInspection(id, name) {
  override def actionFor(holder: ProblemsHolder): PartialFunction[PsiElement, Any] = {
    case typeElement: ScXmlExprImpl =>
      holder.registerProblem(typeElement, message, ERROR, new ReplaceXmlQuickFix(typeElement))
  }
}

class ReplaceXmlQuickFix(token: PsiElement) extends AbstractFixOnPsiElement(name, token) {

  def traverse(elem: ScalaPsiElement): String = elem match {
    case expr: ScXmlExprImpl =>
      println(expr.getText)
      val elems = expr.getElements
      val builder = new StringBuilder
      for (elem <- elems) {
        builder.append(traverse(elem.asInstanceOf[ScalaPsiElement]))
      }
      builder.toString()
    case element: ScXmlElementImpl =>
      println("text is " + element.getText)
      println("toString is " + element.toString)
      element.getNode.getText
    case comment: ScXmlCommentImpl =>
      println("ScXmlCommentImpl")
      comment.getNode.getText
    case cdsect: ScXmlCDSectImpl =>
      println("ScXmlCDSectImpl")
      cdsect.getNode.getText
    case pi: ScXmlPIImpl =>
      println("ScXmlPIImpl")
      pi.getNode.getText
    case _ =>
      println("UNDEF")
      ""
  }

  override def doApplyFix(project: Project): Unit = getElement match {
    case expr: ScXmlExprImpl =>
      val text = traverse(expr)
      val literal = ("\"" * 3) + text + ("\"" * 3)
      expr.replace(createElement(literal, _ => {})(expr.getManager))
  }
}

object XmlExprIsNotSupportedInspection {
  private[xml] val id = "XmpExprIsNotSupported"
  private[xml] val name = InspectionBundle.message("replace.with.string.literal")
  private[xml] val message = "Xml literals are not supported in Dotty"
}
