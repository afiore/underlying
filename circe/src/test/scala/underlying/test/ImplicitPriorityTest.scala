package underlying.test

import io.circe.{Encoder, Json}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FreeSpec, Matchers}
import underlying.circe._

class ImplicitPriorityTest extends FreeSpec with Matchers with TypeCheckedTripleEquals {
  "implicit priority test" - {
    case class Id(value: String)
    object Id {
      implicit val idEnc = implicitly[Encoder[String]].contramap[Id] { s =>
        s"fromCompanion:${s.value}"
      }
    }

    case class IdDerive(value: Int)

    "companion object has priority" in {
      val enc = implicitly[Encoder[Id]]
      enc(Id("x")) should ===(Json.fromString("fromCompanion:x"))
    }

    "automatic derivation" in {
      val enc = implicitly[Encoder[IdDerive]]
      enc(IdDerive(1)) should ===(Json.fromInt(1))
    }
  }

}
