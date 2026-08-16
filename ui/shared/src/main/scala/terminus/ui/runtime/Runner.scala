package terminus.ui.runtime

import cats.effect.IO
import fs2.Stream
import terminus.effect

import java.util.concurrent.BlockingQueue

/** The Runner is responsible for interfacing with the outside world. It runs
  * effects that are supplied by the application or runtime, and provides
  * results of external interactions (such as key presses or timers) to the
  * application. We currently use Cats Effect as the effect system. If there is
  * ever a need to abstract beyond Cats Effect we can extend the interface.
  *
  * Note there are two uses of the term effect:
  *
  *   1. Effects in the reactive system, which are leaves in the graph that
  *      execute everytime their dependencies changes.
  *   2. Effects that interact with the outside world, by doing IO for example.
  *
  * The Runner is responsible for the latter, which we call external effects.
  * However we transform reactive system effects into external effects, so in
  * practice the Runner handles both.
  */
trait Runner:

  /** Schedule an effect for this Runner to run. The effect can do arbitrary
    * work, which will execute within the effect system. The effect must
    * eventually produce a Runnable, such as an Effect. This Runnable will be
    * queued to execute on the Runtime's thread.
    */
  def schedule(io: IO[Runnable]): Unit

  /** Schedule a stream of effects for this Runner to run. The effects can do
    * arbitrary work, which will execute within the effect system. The effects
    * must eventually produce Runnables, such as an Effect. These Runnables will
    * be queued to execute on the Runtime's thread.
    */
  def schedule(stream: Stream[IO, Runnable]): Unit

  /** Run using the dispatch loop on the given runtime. */
  def run(
      terminal: effect.Dimensions & effect.NonBlockingReader
  ): BlockingQueue[Event]
