package underlying

import shapeless.Lazy

package object generic {
  def iso[T, U](implicit ev: Lazy[Iso[T, U]]): Iso[T, U] = ev.value
}
