package terminus

import terminus.effect.Eof
import terminus.effect.Timeout
import scala.concurrent.duration.Duration

trait Peaker {
  def peak(duration: Duration): effect.Peaker ?=> Timeout | Eof | Char =
    effect ?=> effect.peek(duration)
}
