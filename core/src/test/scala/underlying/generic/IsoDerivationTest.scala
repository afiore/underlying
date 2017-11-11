package underlying.generic

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FreeSpec, Matchers}
import shapeless.test.illTyped

class IsoDerivationTest
    extends FreeSpec
    with Matchers
    with TypeCheckedTripleEquals {

  "generic Iso" - {
    case class Id(value: String)
    case class TwoFields(a: Char, b: Int)

    "derive Iso" in {
      val idIso = implicitly[underlying.Iso[Id, String]]
      idIso("x") should ===(Id("x"))
      idIso.underlying(Id("x")) should ===("x")
    }
  }

}
