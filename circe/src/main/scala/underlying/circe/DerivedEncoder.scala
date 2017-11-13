package underlying.circe

import io.circe.{Encoder, Json}
import shapeless._
import underlying.Iso

import scala.annotation.implicitNotFound

@implicitNotFound(
  msg =
    "Cannot derive an underlying Encoder for ${A}: case class has more than one field")
trait DerivedEncoder[A] extends Encoder[A]
object DerivedEncoder {
  def instance[A](f: A => Json) = new DerivedEncoder[A] {
    override def apply(a: A) = f(a)
  }

  implicit def genericUnderlyingEncoder[A <: underlying.NewType[_],
                                        L <: HList,
                                        H](
      implicit
      aGen: Generic.Aux[A, L],
      iso: Lazy[Iso[A, H]],
      hEnc: Lazy[Encoder[H]]): DerivedEncoder[A] = DerivedEncoder.instance[A] {
    a =>
      hEnc.value(iso.value.underlying(a))
  }
}
