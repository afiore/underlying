package underlying

import shapeless._

trait LowPriorityImplicits {
  implicit def genericIso[A, L <: HList, H, T <: HList](
    implicit
    aGen: Generic.Aux[A, L],
    isCons: ops.hlist.IsHCons.Aux[L, H, T],
    selectAll: ops.hlist.SelectAll[H :: HNil, L]
  ): Iso[A, H] =
    Iso.instance[A, H] { s =>
      val l = selectAll(s :: HNil)
      aGen.from(l)
    }(a => aGen.to(a).head)
}
