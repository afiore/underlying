package underlying.circe

import io.circe.{Encoder, Json}
import shapeless._
import ops.hlist.IsHCons
import underlying.Iso

trait DerivedEncoder[A] extends Encoder[A]
object DerivedEncoder {
  def instance[A](f: A => Json) = new DerivedEncoder[A] {
    override def apply(a: A) = f(a)
  }

  implicit def genericUnderlyingEncoder[A, L <: HList, H, T <: HList](
      implicit
      aGen: Generic.Aux[A, L],
      isHCons: IsHCons.Aux[L, H, T],
      iso: Lazy[Iso[A, H]],
      hEnc: Lazy[Encoder[H]]): DerivedEncoder[A] = DerivedEncoder.instance[A] {
    a =>
      hEnc.value(iso.value.underlying(a))
  }
}
