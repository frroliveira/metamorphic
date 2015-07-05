package metamorphic.dsl.model

import scala.reflect.macros.blackbox.Context

trait ModelLiftables {
  implicit val c: Context
  import c.universe._
  
  implicit val liftBasePropertyOptions = Liftable[BasePropertyOptions] { options =>
    q"BasePropertyOptions(${options.isOption}, ${options.isUnique}, ${options.isVisible})"
  }

  implicit val liftRelationPropertyOptions = Liftable[RelationPropertyOptions] { options =>
    q"""RelationPropertyOptions(
      ${options.isOption}, ${options.isUnique}, ${options.isVisible}, ${options.isComposition}, ${options.isReverse})"""
  }
 
  implicit val liftPropertyOptions = Liftable[PropertyOptions] { options =>
    options match {
      case options: BasePropertyOptions => liftBasePropertyOptions.apply(options)
      case options: RelationPropertyOptions => liftRelationPropertyOptions.apply(options)
    }
  }
    
  implicit val liftObjectEnd = Liftable[ObjectEnd] { end =>
    q"ObjectEnd(${end.className})"
  }

  implicit val liftListEnd = Liftable[ListEnd] { end =>
    q"ListEnd(${end.className})"
  }
  
  implicit val liftRelationEnd = Liftable[RelationEnd] { end =>
    end match {
      case end: ListEnd => liftListEnd.apply(end)
      case end: ObjectEnd => liftObjectEnd.apply(end)
    }
  }
  
  implicit val liftString = Liftable[MString] { tpe => q"MString()" }
  
  implicit val liftDouble = Liftable[MDouble] { tpe => q"MDouble()" }

  implicit val liftInteger = Liftable[MInteger] { tpe => q"MInteger()" }

  implicit val liftBoolean = Liftable[MBoolean] { tpe => q"MBoolean()" }

  implicit val liftDate = Liftable[MDate] { tpe => q"MDate()" }

  implicit val liftDateTime = Liftable[MDateTime] { tpe => q"MDateTime()" }
  
  implicit val liftRelation = Liftable[MRelation] { relation =>
    q"MRelation(${relation.end1}, ${relation.end2}, ${relation.reverseProperty})"
  }
  
  implicit val liftType = Liftable[PropertyType] { tpe =>
    tpe match {
      case tpe: MString => liftString.apply(tpe)
      case tpe: MDouble => liftDouble.apply(tpe)
      case tpe: MInteger => liftInteger.apply(tpe)
      case tpe: MBoolean => liftBoolean.apply(tpe)
      case tpe: MDate => liftDate.apply(tpe)
      case tpe: MDateTime => liftDateTime.apply(tpe)
      case tpe: MRelation => liftRelation.apply(tpe)
    }
  }

  implicit val liftProperty = Liftable[Property] { property =>
    q"Property(${property.name}, ${property.tpe}, ${property.options})"
  }
  
  implicit val liftClass = Liftable[Entity] { entity =>
    q"Entity(${entity.name}, ${entity.properties})"
  }

  implicit val liftModel = Liftable[Model] { model =>
    q"Model(${model.entities})"
  }  
}
