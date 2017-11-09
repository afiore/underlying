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
      val idIso = iso[Id, String]
      idIso("x") should ===(Id("x"))
      idIso.underlying(Id("x")) should ===("x")
    }

    "does not derive Iso for classes with more than one field" in {
      illTyped { "implicitly[underlying.Iso[TwoFields, Char]]" }
    }

    "explore other stuff" in {
      import shapeless._

      //def getGen[A, L]: Generic.Aux[A, L]                               =
      //def isHcons[L, H, T](implicit ev: ops.hlist.IsHCons.Aux[L, H, T]) = ev

//      type L = Char :: Int :: HNil
//      val gen   = implicitly[Generic.Aux[TwoFields, L]]
//      val hcons = implicitly[ops.hlist.IsHCons.Aux[L, Char, HNil]]
//      val all   = implicitly[ops.hlist.SelectAll[L, Char :: HNil]]
//      println(hcons)
      pending
    }
  }
}
