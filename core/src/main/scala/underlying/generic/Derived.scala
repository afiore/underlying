package underlying.generic

trait Derived[A] {
  def derived: A
}
object Derived {
  def apply[A](a: A): Derived[A] = new Derived[A] {
    override def derived = a
  }
}
