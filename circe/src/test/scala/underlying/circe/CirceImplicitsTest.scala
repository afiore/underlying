package underlying.circe

import io.circe.{Encoder, Json}
import io.circe.Decoder
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FreeSpec, Matchers}
import shapeless.test.illTyped

class CirceImplicitsTest extends FreeSpec with TypeCheckedTripleEquals with Matchers {
  "circe generics" - {
    case class Id(value: String)
    case class OtherId(toInt: Int)

    object OtherId {
      implicit val otherIdEnc: Encoder[OtherId] = Encoder.instance(id => Json.fromString(s"other-id:$id"))
    }

    case class TwoFields(a: Char, b: Int)

    "derive Encoder" in {
      val enc = implicitly[Encoder[Id]]
      enc(Id("some-id")) should ===(Json.fromString("some-id"))
    }
    "derive Decoder" in {
      Json.fromString("some-id").as[Id] should ===(Right(Id("some-id")))
    }
    "does not derive Encoder for classes with more than one field" in {
      illTyped { "implicitly[Encoder[TwoFields]]" }
    }
    "does not derive Decoder for classes with more than one field" in {
      illTyped { "implicitly[Decoder[TwoField]]" }
    }
  }
}
