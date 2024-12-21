package terminus

trait Mode {
  def raw[F <: effect.Effect, A](f: F ?=> A): (F & effect.Mode[F]) ?=> A =
    effect ?=> effect.raw(f)

}
