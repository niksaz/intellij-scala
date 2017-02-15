package org.jetbrains.sbt.project.data

import java.io.File
import java.net.URI

import com.intellij.openapi.externalSystem.model.{Key, ProjectKeys, ProjectSystemId}
import com.intellij.openapi.externalSystem.model.project.AbstractExternalEntityData
import org.jetbrains.plugins.scala.project.Version
import org.jetbrains.sbt.project.SbtProjectSystem
import org.jetbrains.sbt.project.structure.Play2Keys.AllKeys.ParsedValue
import org.jetbrains.sbt.resolvers.SbtResolver
import SbtEntityData._


abstract class SbtEntityData extends AbstractExternalEntityData(SbtProjectSystem.Id)
object SbtEntityData {
  def datakey[T](clazz: Class[T],
                 weight: Int = ProjectKeys.MODULE.getProcessingWeight + 1
                ): Key[T] = new Key(clazz.getName, weight)
}

/**
  * Data describing a "build" module: The IDEA-side representation of the sbt meta-project
  * @author Pavel Fatin
  */
class SbtBuildModuleData(val imports: Seq[String], val resolvers: Set[SbtResolver]) extends SbtEntityData

object SbtBuildModuleData {
  val Key: Key[SbtBuildModuleData] = datakey(classOf[SbtBuildModuleData])
}


/**
  * Data describing a project which is part of an sbt build.
  * Created by jast on 2016-12-12.
  */
case class SbtModuleData(id: String, buildURI: URI) extends SbtEntityData

object SbtModuleData {
  val Key: Key[SbtModuleData] = datakey(classOf[SbtModuleData])
}


class SbtProjectData(val basePackages: Seq[String],
                     val jdk: Option[Sdk],
                     val javacOptions: Seq[String],
                     val sbtVersion: String,
                     val projectPath: String
                    ) extends SbtEntityData

object SbtProjectData {
  val Key: Key[SbtProjectData] = datakey(classOf[SbtProjectData])
}

sealed trait SbtNamedKey {
  val name: String
}

case class SbtSettingData(name: String, description: String, rank: Int, value: String)
  extends SbtEntityData with SbtNamedKey
object SbtSettingData {
  val Key: Key[SbtSettingData] = datakey(classOf[SbtSettingData])
}

case class SbtTaskData(name: String, description: String, rank: Int)
  extends SbtEntityData with SbtNamedKey
object SbtTaskData {
  val Key: Key[SbtTaskData] = datakey(classOf[SbtTaskData])
}

case class SbtCommandData(name: String, help: Seq[(String,String)])
  extends SbtEntityData with SbtNamedKey
object SbtCommandData {
  val Key: Key[SbtCommandData] = datakey(classOf[SbtCommandData])
}


class ModuleExtData(val scalaVersion: Option[Version],
                    val scalacClasspath: Seq[File],
                    val scalacOptions: Seq[String],
                    val jdk: Option[Sdk],
                    val javacOptions: Seq[String]
                   ) extends SbtEntityData

object ModuleExtData {
  val Key: Key[ModuleExtData] = datakey(classOf[ModuleExtData], ProjectKeys.LIBRARY_DEPENDENCY.getProcessingWeight + 1)
}



class Play2ProjectData(val projectKeys: Map[String, Map[String, ParsedValue[_]]]) extends SbtEntityData
object Play2ProjectData {
  val Key: Key[Play2ProjectData] = datakey(classOf[Play2ProjectData], ProjectKeys.PROJECT.getProcessingWeight + 1)
}

class AndroidFacetData(val version: String, val manifest: File, val apk: File,
                       val res: File, val assets: File, val gen: File, val libs: File,
                       val isLibrary: Boolean, val proguardConfig: Seq[String]) extends SbtEntityData
object AndroidFacetData {
  val Key: Key[AndroidFacetData] = datakey(classOf[AndroidFacetData], ProjectKeys.LIBRARY_DEPENDENCY.getProcessingWeight + 1)
}
