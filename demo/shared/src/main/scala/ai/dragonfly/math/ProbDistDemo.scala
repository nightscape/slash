package ai.dragonfly.math

import ai.dragonfly.democrossy.Demonstration
import ai.dragonfly.math.stats.*
import ai.dragonfly.math.stats.probability.distributions.*
import ai.dragonfly.math.stats.probability.distributions.stream.*


case class ProbDistDemo[DOMAIN](
  name:String,
  dist:ParametricProbabilityDistribution[DOMAIN],
  val histogram: UnivariateHistogram[DOMAIN],
  sampleSize:Int = 10000
) extends Demonstration {
  override def demo(): Unit = {
    println(s"\nDemonstrating $name:\n\tgenerating $sampleSize random variables from $dist\n")
    var i:Int = 0; while (i < sampleSize) {
      histogram(dist.random())
      i += 1
    }
    println(s"\n$histogram\n")
  }
}



case class OnlineProbDistDemo[DOMAIN, PPD <: ParametricProbabilityDistribution[DOMAIN], EPPD <: OnlineUnivariateProbabilityDistributionEstimator[DOMAIN, PPD]](
  name: String,
  idealDist: PPD,
  streamingDist: EPPD,
  sampleSize: Int
)(using `#`: Numeric[DOMAIN]) extends Demonstration {
  override def demo():Unit = {
    println(s"Estimate $name:\n\tSampling: $idealDist")
    val blockSize:Int = sampleSize / 5

    var i:Int = 1
    val end = sampleSize + 1

    while (i < end) {
      streamingDist.observe(idealDist.random())
      if (i % blockSize == 0) {
        println(s"\n\t\testimation after $i samples: ${streamingDist.estimate}")
      }
      i += 1
    }
    println(s"\n\tEstimate: ${streamingDist.estimate}\n\tIdeal Distribution: $idealDist\n")
    println(s"\nTest $idealDist.p($idealDist.random())")
    i = 0
    while (i < 5) {
      val x = idealDist.random()
      println(s"\n\tp($x) = ${idealDist.p(x)}")
      i += 1
    }
    println("\n")
  }
}
