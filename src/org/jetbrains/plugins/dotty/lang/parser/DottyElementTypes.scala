package org.jetbrains.plugins.dotty.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.{PsiElement, PsiFile}
import org.jetbrains.plugins.dotty.DottyLanguage
import org.jetbrains.plugins.dotty.lang.psi.impl.base.types.{DottyAndTypeElementImpl, DottyRefinedTypeElementImpl, DottyTypeArgumentNameElementImpl}
import org.jetbrains.plugins.scala.lang.lexer.ScalaElementType
import org.jetbrains.plugins.scala.lang.parser.ScalaPsiCreator.SelfPsiCreator
import org.jetbrains.plugins.scala.lang.psi.stubs.elements.ScStubFileElementType

/**
  * @author adkozlov
  */
object DottyElementTypes {

  val REFINED_TYPE = new ScalaElementType("Dotty refined type") with SelfPsiCreator {
    override def createElement(node: ASTNode): PsiElement = new DottyRefinedTypeElementImpl(node)
  }
  val WITH_TYPE = new ScalaElementType("Dotty with type") with SelfPsiCreator {
    override def createElement(node: ASTNode): PsiElement = new DottyAndTypeElementImpl(node)
  }
  val TYPE_ARGUMENT_NAME = new ScalaElementType("Dotty type argument name") with SelfPsiCreator {
    override def createElement(node: ASTNode): PsiElement = new DottyTypeArgumentNameElementImpl(node)
  }

  val FILE: IStubFileElementType[_ <: PsiFileStub[_ <: PsiFile]] =
    new ScStubFileElementType(language = DottyLanguage.INSTANCE)
}
