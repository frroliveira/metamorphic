package metamorphic.dsl.generator

import metamorphic.dsl.application._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

object OperationGenerator {

  private val repositoryGenerator = Dependencies.repositoryGenerator.get
  
  def getAllBody(implicit c: Context): Code = {
    import c.universe._
  
    val trees = {
      if (!repositoryGenerator.isAsync)
        q"Response(repository.getAll, Ok)" :: Nil
      else
        q"""
          repository.getAll.map(list =>
            Response(list, Ok)
          )
        """ :: Nil
    }
    
    Code(trees)
  }
  
  def getBody(pathParameter: String)(implicit c: Context): Code = {
    import c.universe._

    val trees = {
      if (!repositoryGenerator.isAsync)
        q"Response(repository.get(${pathParameter.toTerm}), Ok)" :: Nil
      else
        q"""
          repository.get(${pathParameter.toTerm}).map(instance =>
            Response(instance, Ok)
          )
        """ :: Nil
    }
    
    Code(trees)
  }

  def createBody(contentName: String)(implicit c: Context): Code = {
    import c.universe._

    val trees = {
      if (!repositoryGenerator.isAsync)
        q"Response(repository.create(${contentName.toTerm}), Created)" :: Nil
      else
        q"""
          repository.create(${contentName.toTerm}).map(instance =>
            Response(instance, Created)
          )
        """ :: Nil
    }

    Code(trees)
  }
  
  def replaceBody(contentName: String)(implicit c: Context): Code = {
    import c.universe._
    
    val trees = {
      if (!repositoryGenerator.isAsync)
        q"""
          repository.replace(${contentName.toTerm})
          Response(${contentName.toTerm}, Ok)
        """.children
      else
        q"""
          repository.replace(${contentName.toTerm}).map(metamorphicInstance =>
            Response(${contentName.toTerm}, Ok)
          )
        """ :: Nil
    }
    
    Code(trees)
  }
  
  def deleteBody(pathParameter: String)(implicit c: Context): Code = {
    import c.universe._

    val trees = {
      if (!repositoryGenerator.isAsync)
        q"""
          repository.delete(${pathParameter.toTerm})
          Response(None, NoContent)
        """.children
      else
        q"""
          repository.delete(${pathParameter.toTerm}).map(result =>
            Response(None, NoContent)
          )
        """ :: Nil
    }
    
    Code(trees)
  }
  
  def replaceStart(contentName: String, pathParameter: String, copy: String)(implicit c: Context): Code = {
    import c.universe._
    
    Code(q"val ${contentName.toTerm} = ${copy.toTerm}.copy(id = Some(${pathParameter.toTerm}))" :: Nil)
  }
  
  def serviceDependencies(entityName: String)(implicit c: Context): Code = {
    import c.universe._
    
    Code(
      q"""
        import ${Dependencies.repositoryGenerator.get.name.repository.plural.toTerm}.${entityName.repository.toType}
        private val repository = new ${entityName.repository.toType}
      """.children
    )
  }
}
