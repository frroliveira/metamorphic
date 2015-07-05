package metamorphic.dsl.matcher.model

import metamorphic.dsl.matcher.ApplicationMatcher
import metamorphic.dsl.model._
import metamorphic.dsl.model.builder._

import scala.reflect.macros.blackbox.Context

object ModelMatcher {

  def eval(c: Context)(stmt: c.Tree): Model = {

    val stmts = ApplicationMatcher.content(c)(stmt)
    evalList(c)(stmts)
  }
  
  def evalList(c: Context)(stmts: List[c.Tree]): Model = {
  
    val modelBuilder = parseList(c)(stmts)
    completeProperties(modelBuilder)(c)
    modelBuilder.build
  }
  
  def parse(c: Context)(stmt: c.Tree): ModelBuilder = {

    val modelBuilder = new ModelBuilder()
    val entityBuilder = EntityMatcher.parse(c)(stmt)

    entityBuilder match {
      case Some(entityBuilder) => modelBuilder.add(entityBuilder)
      case None =>
    }
    
    modelBuilder
  }
  
  def parseList(c: Context)(stmts: List[c.Tree]): ModelBuilder = {
    
    val modelBuilder = new ModelBuilder()
    modelBuilder.add(EntityMatcher.parseList(c)(stmts))
  }
  
  def completeProperties(modelBuilder: ModelBuilder)(implicit c: Context) = {

    modelBuilder.entityBuilders.foreach(entityBuilder =>
      entityBuilder.propertyBuilders.foreach(propertyBuilder =>
        completeProperty(modelBuilder, propertyBuilder)
      )
    )
  }

  def completeProperty(modelBuilder: ModelBuilder, propertyBuilder: PropertyBuilder)(implicit c: Context) = {
    import c.universe._
  
    def isIncomplete: Boolean = {
      propertyBuilder.build.isReverse
    }

    def currentPos: Position = {
      propertyBuilder.attachment match {
        case tree: Tree => tree.pos
      }
    }
    
    def propertyName: String = {
      propertyBuilder.propertyName;
    }

    def reverseEntityName: String = {
      propertyBuilder.tpe match {
        case MRelation(_, end2, _) => end2.className
      }
    }

    def reversePropertyName: String = {
      propertyBuilder.tpe match {
        case MRelation(_, _, name) => name
      }
    }

    def reverseEntityBuilder: EntityBuilder = {
      modelBuilder.builder(reverseEntityName) match {
        case Some(entityBuilder) => entityBuilder
        case None => c.abort(currentPos, s"ReverseField can't reference non-existent object '$reverseEntityName'.")
      }
    }

    def reversePropertyBuilder: PropertyBuilder = {
      reverseEntityBuilder.builder(reversePropertyName) match {
        case Some(propertyBuilder) => propertyBuilder
        case None => c.abort(currentPos, s"ReverseField can't reference non-existent field '$reversePropertyName' in entity '$reverseEntityName'.")
      }
    }
    
    if (isIncomplete) {
      reversePropertyBuilder.tpe match {
        case MRelation(end1, end2, _) => {
          reversePropertyBuilder.tpe = MRelation(end1, end2, propertyName)
          propertyBuilder.tpe = MRelation(end2, end1, reversePropertyName)
        }
      }
    }
  }
}
