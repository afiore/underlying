package underlying.circe

import io.circe.{Decoder, HCursor}
import shapeless._
import underlying.Iso

import scala.annotation.implicitNotFound
@implicitNotFound(
  msg =
    "Cannot derive an underlying Decoder for ${A}: case class has more than one field")
trait DerivedDecoder[A] extends Decoder[A]
object DerivedDecoder {
  def instance[A](f: HCursor => Decoder.Result[A]): DerivedDecoder[A] =
    new DerivedDecoder[A] {
      override def apply(c: HCursor): Decoder.Result[A] = f(c)
    }

  implicit def genericUnderlyingDecoder[H, A <: AnyVal, L <: HList](
      implicit
      aGen: Generic.Aux[A, L],
      iso: Lazy[Iso[A, H]],
      hDec: Lazy[Decoder[H]]): DerivedDecoder[A] =
    DerivedDecoder.instance[A](c => hDec.value.map(iso.value(_))(c))
}
