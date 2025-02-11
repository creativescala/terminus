package terminus

import terminus.effect.TerminalDimensions

/** Functionalities related to the dimensions of the terminal */
trait Dimensions {
  object dimensions {
    def get: effect.Dimensions ?=> TerminalDimensions = effect ?=>
      effect.getDimensions
  }
}
