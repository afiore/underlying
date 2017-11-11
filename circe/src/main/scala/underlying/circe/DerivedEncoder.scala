package underlying.circe

import io.circe.{Encoder, Json}
import shapeless._
import ops.hlist.IsHCons
import underlying.Iso
import underlying.generic.HasBaseTrait

import scala.annotation.implicitNotFound

@implicitNotFound(
  msg =
    "Cannot derive an underlying Encoder for ${A}: case class has more than one field")
trait DerivedEncoder[A] extends Encoder[A]
object DerivedEncoder {
  def instance[A](f: A => Json) = new DerivedEncoder[A] {
    override def apply(a: A) = f(a)
  }

  implicit def genericUnderlyingEncoder[A, L <: HList, H](
      implicit
      aGen: Generic.Aux[A, L],
      extendsNoSealedTrait: HasBaseTrait.NotFound[A],
      isHCons: IsHCons.Aux[L, H, HNil],
      iso: Lazy[Iso[A, H]],
      hEnc: Lazy[Encoder[H]]): DerivedEncoder[A] = DerivedEncoder.instance[A] {
    a =>
      hEnc.value(iso.value.underlying(a))
  }
}
