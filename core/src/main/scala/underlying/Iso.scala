package underlying

import scala.annotation.implicitNotFound
import shapeless._

@implicitNotFound(
  msg =
    "Cannot derive an Iso for ${T} and ${U}: ${T} has either more than one field or it extends a sealed trait")
trait Iso[T, U] {
  def underlying(t: T): U
  def apply(s: U): T
}

object Iso extends {
  def instance[T, U](from: U => T)(to: T => U): Iso[T, U] = new Iso[T, U] {
    override def underlying(t: T): U = to(t)
    override def apply(s: U): T      = from(s)
  }

  implicit def genericIso[A <: AnyVal, L <: HList, H](
      implicit
      aGen: Generic.Aux[A, L],
      isCons: ops.hlist.IsHCons.Aux[L, H, HNil],
      selectAll: ops.hlist.SelectAll[H :: HNil, L]): Iso[A, H] = {
    Iso.instance[A, H](s => aGen.from(selectAll(s :: HNil)))(a =>
      aGen.to(a).head)
  }
}
