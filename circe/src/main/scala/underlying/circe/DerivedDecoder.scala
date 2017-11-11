package underlying.circe

import io.circe.{Decoder, HCursor}
import shapeless._
import ops.hlist.IsHCons
import underlying.Iso
import underlying.generic.HasBaseTrait

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

  implicit def genericUnderlyingDecoder[A, L <: HList, H](
      implicit
      aGen: Generic.Aux[A, L],
      extendsNoSealedTrait: HasBaseTrait.NotFound[A],
      isHCons: IsHCons.Aux[L, H, HNil],
      iso: Lazy[Iso[A, H]],
      hDec: Lazy[Decoder[H]]): DerivedDecoder[A] =
    DerivedDecoder.instance[A](c => hDec.value.map(iso.value(_))(c))
}
