package underlying.generic

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FreeSpec, Matchers}

case class Id(value: String) extends AnyVal
class IsoDerivationTest
    extends FreeSpec
    with Matchers
    with TypeCheckedTripleEquals {

  "generic Iso" - {
    case class OneField(a: Char)

    "derive Iso" in {
      val idIso = implicitly[underlying.Iso[Id, String]]
      idIso("x") should ===(Id("x"))
      idIso.underlying(Id("x")) should ===("x")
    }

    "doesn't derive iso for non-newtypes" in {
      shapeless.test.illTyped {
        """implicitly[Underlying.Iso[OneField, Char]]"""
      }
    }
  }
}
