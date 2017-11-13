package underlying

trait NewType[A] extends Product1[A] {
  def value: A
  override val _1 = value
}
