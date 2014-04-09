package slamdata.engine.std

import scalaz._
import Validation.{success, failure}
import NonEmptyList.nel

import slamdata.engine.{Transformation, Expansion, Data, SemanticError, Type}

import SemanticError._

trait SetLib extends Library {
  private val Set1Typer = partialTyper {
    case Type.Set(t) :: Nil => t
    case t :: Nil => t
  }
  private val Set1Untyper: (Type => ValidationNel[SemanticError, List[Type]]) = {
    case Type.Set(t) => success(Type.Set(t) :: Nil)
    case t => success(Type.Set(t) :: Nil)
  }

  val Take = Transformation("TAKE", "Takes the first N elements from a set", Type.Top :: Nil,
    Set1Typer,
    Set1Untyper
  )

  val Drop = Transformation("DROP", "Drops the first N elements from a set", Type.Top :: Nil,
    Set1Typer,
    Set1Untyper
  )

  val OrderBy = Transformation("ORDER BY", "Orders a set by the natural ordering of a projection on the set", Type.Top :: Type.Top :: Nil,
    partialTyper {
      case set :: by :: Nil => set
    },
    {
      case Type.Set(t) => success(Type.Set(t) :: Type.Top :: Nil)
      case t => success(Type.Set(t) :: Type.Top :: Nil)
    }
  )

  val Filter = Transformation("WHERE", "Filters a set to include only elements where a projection is true", Type.Top :: Type.Bool :: Nil,
    partialTyper {
      case set :: by :: Nil => set
    },
    {
      case t => success(t :: Type.Bool :: Nil)
    }
  )

  val Cross = Transformation("CROSS", "Computes the Cartesian product of two sets", Type.Top :: Type.Top :: Nil,
    partialTyper {
      case s1 :: s2 :: Nil => s1 & s2
    },
    {
      case Type.Set(t) => success(Type.Set(t) :: Type.Top :: Nil)
      case t => success(Type.Set(t) :: Type.Top :: Nil)
    }
  )

  def functions = Take :: Drop :: OrderBy :: Nil
}
object SetLib extends SetLib