package terminus.effect

trait Mode[+F <: Effect] { self: F =>
  def raw[A](f: F ?=> A): A
}
