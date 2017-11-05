package underlying

import io.circe.{Encoder, Decoder}
import shapeless._

trait CirceLowPriorityImplicits extends LowPriorityImplicits {
  implicit def genericNewTypeEncoder[A, L <: HList, H, T <: HList](
    implicit
    aGen: Generic.Aux[A, L],
    isHCons: ops.hlist.IsHCons.Aux[L, H, T],
    iso: Lazy[Iso[A, H]],
    hEnc: Lazy[Encoder[H]]
  ): Encoder[A] = Encoder.instance[A] { a =>
    hEnc.value(iso.value.underlying(a))
  }

  implicit def genericNewTypeDecoder[A, L <: HList, H, T <: HList](
    implicit
    aGen: Generic.Aux[A, L],
    isHCons: ops.hlist.IsHCons.Aux[L, H, T],
    iso: Lazy[Iso[A, H]],
    hDec: Lazy[Decoder[H]]
  ): Decoder[A] = hDec.value.map(iso.value(_))

}
