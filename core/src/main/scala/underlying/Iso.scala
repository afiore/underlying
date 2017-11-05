package underlying

import scala.annotation.implicitNotFound

@implicitNotFound(msg = "Cannot derive an Iso for ${T} and ${U}: ${T} has more than one field")
trait Iso[T, U] {
  def underlying(t: T): U
  def apply(s: U): T
}

object Iso extends {
  def instance[T, U](from: U => T)(to: T => U): Iso[T, U] = new Iso[T, U] {
    override def underlying(t: T): U = to(t)
    override def apply(s: U): T = from(s)
  }
}
