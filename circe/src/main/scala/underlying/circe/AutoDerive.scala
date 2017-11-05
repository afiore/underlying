package underlying.circe

import io.circe.{Decoder, Encoder}
import shapeless._
import underlying.Iso

trait AutoDerive {
  implicit def genericUnderlyingEncoder[A, L <: HList, H, T <: HList](
      implicit
      aGen: Generic.Aux[A, L],
      isHCons: ops.hlist.IsHCons.Aux[L, H, T],
      iso: Lazy[Iso[A, H]],
      hEnc: Lazy[Encoder[H]]): DerivedEncoder[A] =
    DerivedEncoder.genericUnderlyingEncoder[A, L, H, T]

  implicit def genericUnderlyingDecoder[A, L <: HList, H, T <: HList](
      implicit
      aGen: Generic.Aux[A, L],
      isHCons: ops.hlist.IsHCons.Aux[L, H, T],
      iso: Lazy[Iso[A, H]],
      hDec: Lazy[Decoder[H]]): Decoder[A] =
    DerivedDecoder.genericUnderlyingDecoder[A, L, H, T]
}
