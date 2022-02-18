package ai.dragonfly.math.stats.probability.distributions.stream

import ai.dragonfly.math.stats.probability.distributions
import ai.dragonfly.math.util.{Demonstrable, OnlineProbDistDemo, gamma}

import scala.language.postfixOps
import scala.language.implicitConversions

object Poisson {
  val demo = OnlineProbDistDemo[distributions.Poisson]("Streaming Poisson", distributions.Poisson(69), Poisson(), 10000)
}

class Poisson extends Online[distributions.Poisson] {
  private var minObservation = Double.MaxValue
  private var maxObservation = Double.MinValue

  private var s0 = 0.0 // weighted count
  private var s1 = 0.0 // weighted sum

  /**
   * Assumes only positive valued observations.
   * @param observation the value observed.
   * @param frequency the number of times this value has been observed, default 1L
   */
  override def apply(observation: Double, frequency: Double):Online[distributions.Poisson] = if (observation < 0) {
    throw PoissonDistributionUndefinedForNegativeNumbers(observation)
  } else {
    minObservation = Math.min(observation, minObservation)
    maxObservation = Math.max(observation, maxObservation)

    s0 = s0 + frequency
    s1 = s1 + observation * frequency

    this
  }

  override def min:Double = minObservation
  override def MAX:Double = maxObservation

  def sampleSize:Double = s0

  inline def λ:Double = s1 / s0

  def μ:Double = λ

  inline def `σ²`: Double = λ //  s1 / s0

  def σ:Double = Math.sqrt(λ) // Math.sqrt(`σ²`)

  def p(x:Double):Double = Math.exp( x * Math.log(λ) - λ - Math.log(gamma(x+1)) )

  override def toString: String = s"stream.Poisson(min = $min, MAX = $MAX, λ = μ = σ² = $λ, σ = √λ = $σ, N = $s0)"

  def freeze:distributions.Poisson = distributions.Poisson(λ)

  //  /**
//   * Approximate probability of x, given this Poisson distribution.
//   * @param x a value in the probability distribution
//   * @return P(x)
//   */
//  def P(x:Int):Double = {
//    if (x > max || x < 0) return 0 // ugly hack!
//    val scalar:Double = 100.0 / max
//    val scaledX:Int = Math.round(x*scalar).toInt
//    val lambda:Double = average * scalar
//    (BigDecimal(Math.pow(Math.E, -lambda) * Math.pow(lambda, scaledX)) / (scaledX!) ).toDouble
//  }

  /**
   * Generate a random variable from this Poisson Distribution.
   * @return
   */
  override def random(): Double = distributions.Poisson(λ).random()

    /*
    val scalar:Double = 100.0 / max
    val `λ`:Double = average * scalar
    val `e^-λ` = Math.pow(Math.E, -`λ`)

    val probability:Array[BigDecimal] = new Array[BigDecimal](101)
    var total:BigDecimal = BigDecimal( `e^-λ` * Math.pow(`λ`, 0) ) / (0!)
    var lambdaPower:Double = Math.pow(`λ`, 0)
    probability(0) = total
    for (i <- 1 to 100) {
      lambdaPower = lambdaPower * `λ`
      val term:BigDecimal = BigDecimal( `e^-λ` * lambdaPower ) / (i!)
      total = total + term
      probability(i) = term
    }

    val cumulative:Array[BigDecimal] = new Array[BigDecimal](101)
    cumulative(0) = probability(0)
    for (i <- 1 to 100) {
      cumulative(i) = cumulative(i-1) + probability(i)
    }

    val seed:BigDecimal = total * Random.nextDouble()
    var i = 1
    while(cumulative(i) < seed) {
      i = i + 1
    }
    ((i - 1)*scalar).toInt
  }
 */
}

case class PoissonDistributionUndefinedForNegativeNumbers(negative:Double) extends Exception(s"Poisson distribution undefined for observation: $negative")