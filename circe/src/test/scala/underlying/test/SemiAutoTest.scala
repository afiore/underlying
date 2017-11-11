package underlying.test

import io.circe.{Decoder, Encoder, Json}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FreeSpec, Matchers}
import underlying.circe.semiauto._
import shapeless.test.illTyped
import underlying.generic.HasBaseTrait

class SemiAutoTest extends FreeSpec with Matchers with TypeCheckedTripleEquals {
  case class Id(value: String)
  object Id {
    implicit val idEnc: Encoder[Id] =
      implicitly[Encoder[String]].contramap[Id] { s =>
        s"fromCompanion:${s.value}"
      }
    implicit val Iddec: Decoder[Id] = implicitly[Decoder[String]].map { s =>
      Id(s"fromCompanion:$s")
    }
  }

  case class IdDerive(value: Int)

  object IdDerive {
    implicit val idDeriveEnc: Encoder[IdDerive] =
      deriveUnderlyingEncoder[IdDerive]
    implicit val idDeriveDec: Decoder[IdDerive] =
      deriveUnderlyingDecoder[IdDerive]
  }

  case class OneTwo(one: Int, two: Int)

  sealed trait X
  case class ExtendsX(a: Int) extends X

  "Encoders" - {
    "resolves manually defined" in {
      val enc = implicitly[Encoder[Id]]
      enc(Id("x")) should ===(Json.fromString("fromCompanion:x"))
    }

    "resolves semi-automatic" in {
      val enc = implicitly[Encoder[IdDerive]]
      enc(IdDerive(1)) should ===(Json.fromInt(1))
    }

    "fails on multiple field case class" in {
      illTyped { "deriveUnderlyingEncoder[OneTwo]" }
    }
    "fails on types extending a sealed base trait" in {
      illTyped { "deriveUnderlyingEncoder[ExtendsX]" }
    }
  }

  "Decoders" - {
    "resolves manually defined" in {
      val dec = implicitly[Decoder[Id]]
      Json.fromString("x").as[Id] should ===(Right(Id("fromCompanion:x")))
    }

    "resolves semi-automatic" in {
      val dec = implicitly[Decoder[IdDerive]]
      Json.fromInt(1).as[IdDerive] should ===(Right(IdDerive(1)))
    }
    "fails on multiple field case class" in {
      illTyped { "deriveUnderlyingDecoder[OneTwo]" }
    }
    "fails on types extending a sealed base trait" in {
      illTyped { "deriveUnderlyingDecoder[ExtendsX]" }
    }
  }
}
