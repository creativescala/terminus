package terminus

/** Functionality for getting the dimensions of the terminal */
trait Dimensions {
  object dimensions {
    def getDimensions: effect.Dimensions ?=> Unit = effect ?=> 
      effect.getDimensions
  }
}
