package terminus.ui.capability

import terminus.ui.react.Effect

/** The capability to schedule running reactive effects. */
trait Schedule:
  /** Schedule this effect to run once. */
  def schedule(effect: Effect): Unit
