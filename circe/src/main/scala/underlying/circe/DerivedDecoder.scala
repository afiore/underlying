package underlying.circe

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import shapeless._
import ops.hlist.IsHCons
import underlying.Iso

trait DerivedDecoder[A] extends Decoder[A]
object DerivedDecoder {
  def instance[A](f: HCursor => Decoder.Result[A]): DerivedDecoder[A] =
    new DerivedDecoder[A] {
      override def apply(c: HCursor): Result[A] = f(c)
    }

  implicit def genericUnderlyingDecoder[A, L <: HList, H, T <: HList](
      implicit
      aGen: Generic.Aux[A, L],
      isHCons: IsHCons.Aux[L, H, T],
      iso: Lazy[Iso[A, H]],
      hDec: Lazy[Decoder[H]]): DerivedDecoder[A] =
    DerivedDecoder.instance[A](c => hDec.value.map(iso.value(_))(c))
}
