/*
 * Copyright 2023 dragonfly.ai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.dragonfly.math.stats.probability.distributions

import ai.dragonfly.math.interval.{Domain, Interval}
import ai.dragonfly.math.stats.probability.distributions.Sampleable

// ℕ population size symbol for future reference.

trait ProbabilityDistribution[DOMAIN: Numeric] extends Sampleable[DOMAIN] {
  val `#` = implicitly[Numeric[DOMAIN]]
  import `#`._

  /**
   * Probability Densidy Function: PDF
   * Computes the probability of drawing sample x from this distribution.
   *
   * @param x a sample
   * @return Probability(x)
   */
  def p(x: DOMAIN): Double

  //  /**
  //   * Cumulative Density Function: CDF
  //   * Computes the probability of drawing a sample less than or equal to x from this distribution.
  //   * @param x a sample
  //   * @return Probability(i <= x) for all i
  //   */
  //  TODO: maybe someday.
  //  def cumulative(x:Double):Double

}


trait ParametricProbabilityDistribution[DOMAIN] extends ProbabilityDistribution[DOMAIN] {
  def μ: Double
  def mean: Double = μ

  def `σ²`: Double
  def variance: Double = `σ²`

  def σ: Double
  def standardDeviation: Double = σ
}
