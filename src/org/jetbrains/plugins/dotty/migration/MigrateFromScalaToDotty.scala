package org.jetbrains.plugins.dotty.migration

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys, LangDataKeys}
import org.jetbrains.plugins.dotty.codeInspection.lazyVal.LazyValNotVolatileInspection.isApplicableToDef
import org.jetbrains.plugins.dotty.codeInspection.procedure.ProcedureSyntaxQuickFix
import org.jetbrains.plugins.dotty.codeInspection.xml.ReplaceXmlPatternQuickFix
import org.jetbrains.plugins.dotty.codeInspection.lazyVal.LazyValNotVolatileQuickFix
import org.jetbrains.plugins.scala.codeInspection.xml.XmlLiteralToInterpolatedStringTransformation
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.kDEF
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition
import org.jetbrains.plugins.scala.lang.psi.impl.expr.xml.{ScXmlExprImpl, ScXmlPatternImpl}
import org.jetbrains.plugins.scala.lang.psi.impl.statements.ScPatternDefinitionImpl
import org.jetbrains.plugins.scala.project._
import org.jetbrains.plugins.scala.util.ScalaUtils

/**
  * @author niksaz
  */
class MigrateFromScalaToDotty extends AnAction {
  def traverseAndApplyFixes(rootElement: ScalaPsiElement): Unit = {
    rootElement match {
      case expr: ScXmlExprImpl =>
        new XmlLiteralToInterpolatedStringTransformation(expr).doApplyFix(rootElement.getProject)
        return
      case pattern: ScXmlPatternImpl =>
        new ReplaceXmlPatternQuickFix(pattern).doApplyFix(rootElement.getProject)
        return
      case patternDef: ScPatternDefinitionImpl if isApplicableToDef(patternDef) =>
        new LazyValNotVolatileQuickFix(patternDef).doApplyFix(rootElement.getProject)
      case funcDef: ScFunctionDefinition if !funcDef.hasAssign =>
        funcDef.findChildrenByType(kDEF).foreach { token =>
          new ProcedureSyntaxQuickFix(token).doApplyFix(rootElement.getProject)
        }
      case _ =>
    }
    for (child <- rootElement.getChildren) {
      child match {
        case scPsi: ScalaPsiElement =>
          traverseAndApplyFixes(scPsi)
        case _ =>
      }
    }
  }

  override def actionPerformed(e: AnActionEvent): Unit = {
    var elements = LangDataKeys.PSI_ELEMENT_ARRAY.getData(e.getDataContext)
    if (elements == null) {
      val file = CommonDataKeys.PSI_FILE.getData(e.getDataContext)
      if (file != null) elements = Array(file)
      else elements = Array.empty
    }
    for (element <- elements) {
      element.getContainingFile match {
        case scFile: ScalaFile if scFile.isInDottyModule =>
          ScalaUtils.runWriteAction(new Runnable {
            override def run(): Unit = traverseAndApplyFixes(scFile)
          }, scFile.getProject, "Migrate to Dotty")
      }
    }
  }
}
