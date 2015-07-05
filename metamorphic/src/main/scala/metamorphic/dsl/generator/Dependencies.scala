package metamorphic.dsl.generator

import metamorphic.Settings
import metamorphic.dsl.util.Instantiator

object Dependencies {

  val repositoryGenerator = 
    Instantiator.instance[RepositoryGenerator](Settings.repositoryGenerator)
  
  val serviceGenerator =
    Instantiator.instance[ServiceGenerator](Settings.serviceGenerator)
}
