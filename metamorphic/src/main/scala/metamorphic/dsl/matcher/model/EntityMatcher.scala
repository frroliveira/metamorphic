package metamorphic.dsl.matcher.model

import metamorphic.dsl.model.Entity
import metamorphic.dsl.model.builder.EntityBuilder

import scala.reflect.macros.blackbox.Context

object EntityMatcher {

  def eval(c: Context)(stmt: c.Tree): Option[Entity] = {

    val entityBuilder = parse(c)(stmt)
    
    entityBuilder match {
      case Some(entityBuilder) => Some(entityBuilder.build)
      case None => None
    }
  }
  
  def evalList(c: Context)(stmts: List[c.Tree]): List[Entity] = {
  
    stmts
      .map(stmt => eval(c)(stmt))
      .collect { case Some(entity) => entity }
  }
  
  def parse(c: Context)(stmt: c.Tree): Option[EntityBuilder] = {
    import c.universe._

    stmt match {
      case q"$_ class $clasz { ..$body }" => {
       
        val entityName = clasz.toString
        val entityBuilder = new EntityBuilder(entityName)

        body.foreach(propertyTree => {
          PropertyMatcher.parse(c)(propertyTree, entityName) match {
            case Some(propertyBuilder) => entityBuilder.add(propertyBuilder)
            case None =>
          }
        })
        
        Some(entityBuilder)
      }
      case _ => None
    }
  }
  
  def parseList(c: Context)(stmts: List[c.Tree]): List[EntityBuilder] = {
    
    stmts
      .map(stmt => parse(c)(stmt))
      .collect { case Some(builder) => builder }
  }
}
