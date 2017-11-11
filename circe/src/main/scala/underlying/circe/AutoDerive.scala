package underlying.circe

import io.circe.{Decoder, Encoder}
import shapeless._
import underlying.Iso
import underlying.generic.HasBaseTrait

trait AutoDerive {
  implicit def genericUnderlyingEncoder[A, L <: HList, H](
      implicit
      aGen: Generic.Aux[A, L],
      extendsNoSealedTrait: HasBaseTrait.NotFound[A],
      isHCons: ops.hlist.IsHCons.Aux[L, H, HNil],
      iso: Lazy[Iso[A, H]],
      hEnc: Lazy[Encoder[H]]): DerivedEncoder[A] =
    DerivedEncoder.genericUnderlyingEncoder[A, L, H]

  implicit def genericUnderlyingDecoder[A, L <: HList, H](
      implicit
      aGen: Generic.Aux[A, L],
      extendsNoSealedTrait: HasBaseTrait.NotFound[A],
      isHCons: ops.hlist.IsHCons.Aux[L, H, HNil],
      iso: Lazy[Iso[A, H]],
      hDec: Lazy[Decoder[H]]): Decoder[A] =
    DerivedDecoder.genericUnderlyingDecoder[A, L, H]
}
