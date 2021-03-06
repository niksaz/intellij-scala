package org.jetbrains.plugins.scala
package testingSupport.test.specs2

import com.intellij.execution.configurations._
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScObject}
import org.jetbrains.plugins.scala.testingSupport.test._

/**
  * @author Ksenia.Sautina
  * @since 5/17/12
  */

class Specs2RunConfiguration(override val project: Project,
                             override val configurationFactory: ConfigurationFactory,
                             override val name: String)
  extends AbstractTestRunConfiguration(project, configurationFactory, name, TestConfigurationUtil.specs2ConfigurationProducer) {

  override def suitePaths: List[String] = Specs2Util.suitePaths

  override def mainClass = "org.jetbrains.plugins.scala.testingSupport.specs2.JavaSpecs2Runner"

  override def reporterClass = "org.jetbrains.plugins.scala.testingSupport.specs2.JavaSpecs2Notifier"

  override def errorMessage: String = "Specs2 is not specified"

  override def currentConfiguration: Specs2RunConfiguration = Specs2RunConfiguration.this

  protected[test] override def isInvalidSuite(clazz: PsiClass): Boolean = Specs2RunConfiguration.isInvalidSuite(clazz, getSuiteClass)

  override protected def sbtClassKey = " -- -specname "
  override protected def sbtTestNameKey = " -- -ex "

  override def allowsSbtUiRun: Boolean = false
}

object Specs2RunConfiguration extends SuiteValidityChecker {
  private def isScalaObject(clazz: PsiClass) = clazz.getQualifiedName.endsWith("$")

  override protected[test] def lackSuitableConstructor(clazz: PsiClass): Boolean =
    !isScalaObject(clazz) && AbstractTestRunConfiguration.lackSuitableConstructor(clazz)

  override protected[test] def isInvalidClass(clazz: PsiClass): Boolean = !clazz.isInstanceOf[ScClass] && !clazz.isInstanceOf[ScObject]
}