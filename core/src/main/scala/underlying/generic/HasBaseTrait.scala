package underlying.generic

import scala.annotation.implicitNotFound
import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.macros.blackbox

trait HasBaseTrait[+A] {
  def found: Boolean
}

object HasBaseTrait {
  class Found[A] extends HasBaseTrait[A] {
    override val found = true
  }
  object Found {
    implicit def baseTraitFoundFor[A]: Found[A] =
      macro applyImpl[A, Found[A]]
  }

  @implicitNotFound(msg = "${A} extends a sealed base trait")
  class NotFound[A] extends HasBaseTrait[A] {
    override val found = false
  }

  object NotFound {
    implicit def baseTraitNotFoundFor[A]: NotFound[A] =
      macro applyImpl[A, NotFound[A]]
  }

  def applyImpl[A, Out <: HasBaseTrait[A]](c: blackbox.Context)(
      implicit tag: c.WeakTypeTag[A]): c.Expr[Out] = {
    import c.universe._

    val a           = weakTypeOf[A](tag).typeSymbol
    val baseClasses = a.asClass.baseClasses
    val maybeBaseTrait =
      baseClasses
        .find(t => t.asClass.isTrait && t.asClass.isSealed)
        .map(_.asType.name)

    //println(s"got it: ${maybeBaseTrait}")
    val out = maybeBaseTrait
      .map(t => q"new HasBaseTrait.Found[$a]")
      .getOrElse(q"new HasBaseTrait.NotFound[$a]")

    c.Expr[Out](q"$out")
  }
}
