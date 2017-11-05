package underlying.circe

import io.circe.{Decoder, Encoder}

package object semiauto {
  def deriveUnderlyingDecoder[A](implicit dec: DerivedDecoder[A]): Decoder[A] =
    dec
  def deriveUnderlyingEncoder[A](implicit enc: DerivedEncoder[A]): Encoder[A] =
    enc
}
