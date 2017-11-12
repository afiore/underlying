package underlying.circe

package object auto extends AutoDerive {
  val HasBaseTrait = underlying.generic.HasBaseTrait
  type HasBaseTrait[+A] = underlying.generic.HasBaseTrait[A]
}
