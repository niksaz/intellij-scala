package org.jetbrains.plugins.dotty.codeInspection.xml

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.dotty.codeInspection.xml.XmlExprIsNotSupportedInspection.{id, message}
import org.jetbrains.plugins.scala.codeInspection.AbstractInspection
import org.jetbrains.plugins.scala.lang.psi.impl.expr.xml.ScXmlExprImpl

/**
  * @author niksaz
  */
class XmlExprIsNotSupportedInspection extends AbstractInspection(id, message) {
  override def actionFor(holder: ProblemsHolder): PartialFunction[PsiElement, Any] = {
    case typeElement: ScXmlExprImpl =>
      holder.registerProblem(typeElement, message, ERROR)
  }
}

object XmlExprIsNotSupportedInspection {
  private[xml] val id = "XmpExprIsNotSupported"
  private[xml] val message = s"Xml literals are not supported in Dotty"
}
