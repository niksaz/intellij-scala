package org.jetbrains.sbt.project.data.service

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import org.jetbrains.sbt.project.data.SbtBuildModuleData
import org.jetbrains.sbt.project.module.SbtModule
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.sbt.resolvers.SbtResolver

/**
 * @author Pavel Fatin
 */
class SbtBuildModuleDataService extends AbstractDataService[SbtBuildModuleData, Module](SbtBuildModuleData.Key) {
  override def createImporter(toImport: Seq[DataNode[SbtBuildModuleData]],
                              projectData: ProjectData,
                              project: Project,
                              modelsProvider: IdeModifiableModelsProvider): Importer[SbtBuildModuleData] =
    new SbtBuildModuleDataService.Importer(toImport, projectData, project, modelsProvider)
}

object SbtBuildModuleDataService {
  private class Importer(dataToImport: Seq[DataNode[SbtBuildModuleData]],
                         projectData: ProjectData,
                         project: Project,
                         modelsProvider: IdeModifiableModelsProvider)
    extends AbstractImporter[SbtBuildModuleData](dataToImport, projectData, project, modelsProvider) {

    override def importData(): Unit =
      dataToImport.foreach { moduleNode =>
        for {
          module <- getIdeModuleByNode(moduleNode)
          imports = moduleNode.getData.imports
          resolvers = moduleNode.getData.resolvers
        } {
          SbtModule.setImportsTo(module, imports)
          setResolvers(module, resolvers)
        }
      }

    private def setResolvers(module: Module, resolvers: Set[SbtResolver]): Unit = {
      SbtModule.setResolversTo(module, resolvers)
    }

  }
}
