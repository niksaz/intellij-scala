package org.jetbrains.plugins.dotty.lang.parser

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.jetbrains.plugins.scala.lang.parser.ScalaParserDefinition

/**
  * @author adkozlov
  */
class DottyParserDefinition extends ScalaParserDefinition {

  override def createParser(project: Project): DottyParser = new DottyParser

  override def getFileNodeType: IStubFileElementType[_ <: PsiFileStub[_ <: PsiFile]] =
    DottyElementTypes.FILE
}
