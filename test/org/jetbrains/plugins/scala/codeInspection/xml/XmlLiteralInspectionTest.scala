package org.jetbrains.plugins.scala.codeInspection.xml

import com.intellij.codeInspection.LocalInspectionTool
import org.jetbrains.plugins.scala.codeInspection.xml.XmlLiteralInspection._
import org.jetbrains.plugins.scala.codeInspection.{InspectionBundle, ScalaQuickFixTestBase}

/**
  * @author niksaz
  */
class XmlLiteralInspectionTest extends ScalaQuickFixTestBase {

  override protected val classOfInspection: Class[_ <: LocalInspectionTool] = classOf[XmlLiteralInspection]

  override protected val description: String = message

  protected val hint: String = InspectionBundle.message("replace.with.interpolated.string")

  def testOneLevelXml(): Unit = {
    val text = "val c = <a>hello</a>"
    val expected = "val c = xml\"\"\"<a>hello</a>\"\"\""
    testQuickFix(text, expected, hint)
  }

  def testMultiLevelXml(): Unit = {
    val text =
      """
        def getXml(): Unit =
        <pizza>
        <crust type="thin" size="14" />
        <topping>cheese</topping>
        <topping>sausage</topping>
        </pizza>
      """
    val expected =
      s"""
        def getXml(): Unit =
        xml\"\"\"<pizza>
        <crust type="thin" size="14" />
        <topping>cheese</topping>
        <topping>sausage</topping>
        </pizza>\"\"\"
      """
    testQuickFix(text, expected, hint)
  }

}

