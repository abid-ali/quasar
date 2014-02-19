package slamdata.engine

import slamdata.engine.analysis._
import slamdata.engine.std.Library

import scalaz._
import scalaz.std.map._

import scalaz.syntax.apply._

trait SemanticAnalysis {
  import slamdata.engine.sql._
  import SemanticError._

  private type Failure = NonEmptyList[SemanticError]

  private def fail[A](e: SemanticError) = Validation.failure[NonEmptyList[SemanticError], A](NonEmptyList(e))
  private def succeed[A](s: A) = Validation.success[NonEmptyList[SemanticError], A](s)

  def tree(root: Node): AnnotatedTree[Node, Unit] = AnnotatedTree.unit(root, n => n.children)

  /**
   * This analyzer looks for function invocations (including operators), 
   * and binds them to their associated function definitions in the
   * provided library. If a function definition cannot be found, 
   * produces a semantic error with details on the failure.
   */
  def FunctionBind[A](library: Library) = {
    def findFunction(name: String) = {
      library.functions.find(f => f.name == name).map(f => Validation.success(Some(f))).getOrElse(
        fail(FunctionNotFound(name))
      )
    }

    Analysis.annotate[Node, A, Option[Func], Failure] { 
      case (InvokeFunction(name, args)) => findFunction(name)          

      case (Unop(expr, op)) => findFunction(op.name)

      case (Binop(left, right, op)) => findFunction(op.name)

      case _ => Validation.success(None)
    }
  }

  /**
   * This analyzer works bottom-up to infer the type of all expressions.
   * If a type is inferred to have contradictory constraints, a type 
   * error will be produced that contains details on the contradiction.
   */
  def TypeInfer = {
    Analysis.readTree[Node, Option[Func], Type \/ Data, Failure] { tree =>
      Analysis.join[Node, Option[Func], Type \/ Data, Failure]((getAnn, node) => {
        def succeed(v: Type \/ Data): ValidationNel[SemanticError, Type \/ Data] = Validation.success(v)
        def yieldType(tpe: Type): ValidationNel[SemanticError, Type \/ Data] = succeed(-\/(tpe))
        def yieldData(data: Data): ValidationNel[SemanticError, Type \/ Data] = succeed(\/-(data))

        def fail(error: SemanticError): ValidationNel[SemanticError, Type \/ Data] = Validation.failure(NonEmptyList(error))

        def func(node: Node): ValidationNel[SemanticError, Func] = {
          tree.attr(node).map(Validation.success).getOrElse(Validation.failure(NonEmptyList(FunctionNotBound(node))))
        }

        def propagate(n: Node) = succeed(getAnn(n))

        def typeOf(n: Node) = getAnn(n).fold(identity, _.dataType)

        node match {
          case SelectStmt(projections, relations, filter, groupBy, orderBy, limit, offset) =>
            // TODO: Use object instead of array so we can hang onto names:
            yieldType(Type.makeArray(projections.map(typeOf)))

          case Proj(expr, alias) => propagate(expr)

          case Subselect(select) => propagate(select)

          case SetLiteral(values) => yieldType(Type.makeArray(values.map(typeOf)))

          case Wildcard => yieldType(Type.Top)

          case Binop(left, right, op) =>
            // TODO:

            ???

          case Unop(expr, op) => ???

          case Range(lower, upper) => ???

          case FieldIdent(qualifier, name) => ???

          case InvokeFunction(name, args) => ???

          case CaseExpr(expr, cases, default) =>  ???

          case CaseExprCase(cond, expr) => ???

          case CaseWhenExpr(cases, default) => ???

          case IntLiteral(value) => ???

          case FloatLiteral(value) => ???

          case StringLiteral(value) => ???

          case NullLiteral() => ???

          case TableRelationAST(name, alias) => ???

          case SubqueryRelationAST(subquery, alias) => ???

          case JoinRelation(left, right, tpe, clause) => ???

          case GroupBy(keys, having) => ???

          case OrderBy(keys) => ???

          case _ : BinaryOperator => ???

          case _ : UnaryOperator => ???
        }
      })
    }
  }

}
object SemanticAnalysis extends SemanticAnalysis