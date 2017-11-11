package underlying.generic

import org.scalatest.{FreeSpec, Matchers}

class HasBaseTraitTest extends FreeSpec with Matchers {
  "BaseTraitCheck macro" - {

    sealed trait X
    case class Y(a: Int) extends X
    case class Id(value: String)

    "can determine that a type has a base trait" in {
      val check = implicitly[HasBaseTrait.Found[Y]]
      check.found should ===(true)
    }

    "can determine that a type has no base trait" in {
      val check = implicitly[HasBaseTrait.NotFound[Id]]
      check.found should ===(false)
    }

    "fails at compile-time if a type has a base trait" in {
      shapeless.test.illTyped {
        """implicitly[BaseTraitCheck.NotFound[Y]]"""
      }
    }
  }
}
