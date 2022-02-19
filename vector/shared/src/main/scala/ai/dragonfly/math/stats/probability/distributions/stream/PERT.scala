package ai.dragonfly.math.stats.probability.distributions.stream

import ai.dragonfly.math.stats.probability.distributions
import ai.dragonfly.math.stats.probability.distributions.ProbabilityDistribution
import ai.dragonfly.math.examples.Demonstrable

object PERT {
  val doNotUse:String = "" +
    "As a special case of the Beta distribution, PERT only has utility in applications with unknowable σ².\n" +
    "In such situations, PERT approximates σ² with the huristic value: σ² = ((μ - min) * (MAX - μ)) / 7.0.\n" +
    "Because stream.Beta can approximate σ² directly, its accuracy always meets or exceeds that of streem.PERT.\n\n" +
    "One should always prefer stream.Beta, but if you insist on using stream.PERT, the UseBetaDistributionInstead\n" +
    "exception generated by its constructor contains a reference to a functioning instance of stream.PERT:\n\n" +
    "val onlinePERT = try {\n" +
      "\tnew ai.dragonfly.math.stats.probability.distributions.stream.PERT\n" +
    "} catch {\n" +
      "\t// I understand the superiority of stream.Beta over stream.PERT, but I have reasons!\n" +
      "\tcase ai.dragonfly.math.stats.probability.distributions.stream.UseBetaDistributionInstead(pert) => pert\n" +
    "}"

  val demo = new Demonstrable {
    override def demo(implicit sb: StringBuilder): StringBuilder = {
      sb.append(doNotUse)
    }

    override def name: String = "stream.PERT"
  }
}

/**
 * As a special case of the Beta distribution, PERT only has utility in applications with unknowable σ².
 * In such situations, PERT approximates σ² with the huristic value: σ² = ((μ - min) * (MAX - μ)) / 7.0.
 * Because stream.Beta can approximate σ² directly, its accuracy always meets or exceeds that of streem.PERT.

 * One should always prefer stream.Beta, but if you insist on using stream.PERT, the UseBetaDistributionInstead
 * exception generated by its constructor contains a reference to a functioning instance of stream.PERT:
 * val onlinePERT = try {
 * 	new ai.dragonfly.math.stats.probability.distributions.stream.PERT
 * } catch {
 * 	// I understand the superiority of stream.Beta over stream.PERT, but I have reasons!
 * 	case ai.dragonfly.math.stats.probability.distributions.stream.UseBetaDistributionInstead(pert) => pert
 * }
 */


class PERT extends OnlineContinuous {
  private var minObservation = Double.MaxValue
  private var maxObservation = Double.MinValue

  private var s0 = 0.0
  private var s1 = 0.0

  def apply(observation: Double, frequency: Double = 1.0):PERT = {
    minObservation = Math.min(observation, minObservation)
    maxObservation = Math.max(observation, maxObservation)

    s0 = s0 + frequency
    s1 = s1 + observation * frequency

    this
  }

  override def min:Double = minObservation
  override def MAX:Double = maxObservation

  override def n:Double = s0

  override def μ: Double = s1 / s0
  override def `σ²`: Double = ((μ - min) * (MAX - μ)) / 7.0
  override def σ: Double = Math.sqrt(`σ²`)

  override def freeze: distributions.PERT = distributions.PERT(minObservation, s1 / s0, maxObservation)

  override def p(x: Double):Double = freeze.p(x)
  override def random():Double = freeze.random()

  override def toString:String = s"stream.PERT(min = $min, MAX = $MAX, μ = $μ, σ² = ${`σ²`}, σ = $σ, N = $s0)"

  throw new UseBetaDistributionInstead(this)
}

case class UseBetaDistributionInstead(pert:PERT) extends Exception(PERT.doNotUse)