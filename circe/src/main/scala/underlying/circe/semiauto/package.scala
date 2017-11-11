package underlying.circe

import io.circe.{Decoder, Encoder}
import underlying.generic.HasBaseTrait

package object semiauto {
  def deriveUnderlyingDecoder[A](implicit
                                 noSbt: HasBaseTrait.NotFound[A],
                                 dec: DerivedDecoder[A]): Decoder[A] = dec

  def deriveUnderlyingEncoder[A](implicit
                                 noSbt: HasBaseTrait.NotFound[A],
                                 enc: DerivedEncoder[A]): Encoder[A] = enc
}
