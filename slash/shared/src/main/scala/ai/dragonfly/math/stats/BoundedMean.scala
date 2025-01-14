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

package ai.dragonfly.math.stats

import ai.dragonfly.math.interval.Interval

object BoundedMean {
  def apply[DOMAIN: Numeric](μ:Double, bounds: Interval[DOMAIN], ℕ:DOMAIN):BoundedMean[DOMAIN] = {
    if (bounds.rangeContains(μ)) new BoundedMean[DOMAIN](μ, bounds, ℕ)
    else throw MeanOutsideBounds[DOMAIN](μ, bounds)
  }
}

case class BoundedMean[DOMAIN: Numeric](μ:Double, bounds: Interval[DOMAIN], ℕ:DOMAIN) {
  def min:DOMAIN = bounds.min
  def MAX:DOMAIN = bounds.MAX
}

case class MeanOutsideBounds[DOMAIN](μ:Double, bounds: Interval[DOMAIN]) extends Exception(
  s"Cannot create BoundedMean(μ = $μ, bounds = $bounds).  $μ lies outside of $bounds."
)