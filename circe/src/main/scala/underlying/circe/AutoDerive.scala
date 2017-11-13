package underlying.circe

import io.circe.{Decoder, Encoder}
import shapeless._
import underlying.Iso

trait AutoDerive {
  implicit def genericUnderlyingEncoder[H,
                                        A <: underlying.NewType[H],
                                        L <: HList](
      implicit
      aGen: Generic.Aux[A, L],
      isHCons: ops.hlist.IsHCons.Aux[L, H, HNil],
      iso: Lazy[Iso[A, H]],
      hEnc: Lazy[Encoder[H]]): DerivedEncoder[A] =
    DerivedEncoder.genericUnderlyingEncoder[A, L, H]

  implicit def genericUnderlyingDecoder[H,
                                        A <: underlying.NewType[H],
                                        L <: HList](
      implicit
      aGen: Generic.Aux[A, L],
      isHCons: ops.hlist.IsHCons.Aux[L, H, HNil],
      iso: Lazy[Iso[A, H]],
      hDec: Lazy[Decoder[H]]): Decoder[A] =
    DerivedDecoder.genericUnderlyingDecoder[H, A, L]
}
