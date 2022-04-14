package ai.dragonfly.math.vector

import ai.dragonfly.math.*

import scala.scalajs.js

import scala.util.Random

object Vector {

  def fill(dimension:Int)(d: Double): Vector[_] = apply(VectorValues.fill(dimension)(d))

  def tabulate(dimension:Int)(f: Int => Double): Vector[_] = apply(VectorValues.tabulate(dimension)(f))

  def apply(values: VectorValues): Vector[_] = {
    values.length match {
      case dim if dim < 2 => throw UnsupportedVectorDimension(dim)
      case 2 => Vector2(values(0), values(1))
      case 3 => Vector3(values(0), values(1), values(2))
      case 4 => Vector4(values(0), values(1), values(2), values(3))
      case _ => VectorN(values)
    }
  }

  def apply(d:Double*): Vector[_] = {
    d.size match {
      case dim if dim < 2 => throw UnsupportedVectorDimension(dim)
      case 2 => Vector2(d(0), d(1))
      case 3 => Vector3(d(0), d(1), d(2))
      case 4 => Vector4(d(0), d(1), d(2), d(3))
      case _ => VectorN(VectorValues(d:_*))
    }
  }

  def midpoint[V <: VectorData with Vector[V]](v0: V, v1: V):V = ((v0 + v1) * 0.5)

  def mean[V <: VectorData with Vector[V]](`[v₁v₂⋯vₙ]`:V*):V = {
    val μ:V = `[v₁v₂⋯vₙ]`.head.copy()
    for (v <- `[v₁v₂⋯vₙ]`.tail) {
      μ += v
    }
    μ /= `[v₁v₂⋯vₙ]`.size
  }

  def mean[V <: VectorData with Vector[V]](`[v₀v₁⋯v₍ₙ₋₁₎]`: VECTORS):V = {
    val μ:V = `[v₀v₁⋯v₍ₙ₋₁₎]`(0).asInstanceOf[V].copy()
    for (i <- 1 to `[v₀v₁⋯v₍ₙ₋₁₎]`.length) {
      μ += μ.recognize(`[v₀v₁⋯v₍ₙ₋₁₎]`(i))
    }
    μ /= `[v₀v₁⋯v₍ₙ₋₁₎]`.length
  }

}

// trait for Vector Companion Objects
trait VectorCompanion[V <: VectorData with Vector[V]] {

  def validDimension(dimension:Int):Boolean

  def apply(values: VectorValues): V

  protected def fill(value: Double)(using dimension:Int):V = apply(VectorValues.fill(dimension)(value))

  protected def tabulate(f: Int => Double)(using dimension:Int):V = apply(VectorValues.tabulate(dimension)(f))

  def blend(alpha: Double, v0: V, v1: V):V = ((v0 * alpha) + (v1 * (1.0 - alpha))).asInstanceOf[V]

  def mean(`[v₁v₂⋯vₙ]`:V*):V = Vector.mean[V](`[v₁v₂⋯vₙ]`:_*)

  def mean(`[v₀v₁⋯v₍ₙ₋₁₎]`: VECTORS):V = Vector.mean(`[v₀v₁⋯v₍ₙ₋₁₎]`).asInstanceOf[V]

  def midpoint(v0: V, v1: V):V = blend(0.5, v0, v1)
}

trait Vector[V] extends DenseVectorData {

  type VEC = V with Vector[V]

  // abstract
  def copy():VEC

  // copy operators
  def *(scalar:Double):VEC = copy().scale(scalar)
  def /(divisor:Double):VEC = copy().divide(divisor)

  def +(v0:VEC):VEC = copy().add(v0)
  def -(v0:VEC ):VEC = copy().subtract(v0)

  // implemented
  inline def round():VEC = {
    euclid.round()
    recognize(this)
  }

  inline def discretize():VEC = round()

  inline def discretize(r:Double):VEC = {
    euclid.discretize(r)
    recognize(this)
  }

  inline def normSquared: Double = {
    euclid.normSquared
  }

  inline def norm: Double = {
    euclid.norm
  }


  inline def dot(v0:VEC): Double = {
    euclid.dot(v0)
  }

  inline def scale(scalar: Double):VEC = {
    euclid.scale(scalar)
    recognize(this)
  }

  inline def divide(divisor: Double):VEC = {
    euclid.divide(divisor)
    recognize(this)
  }

  inline def add(v0:VEC):VEC = {
    euclid.add(v0)
    recognize(this)
  }

  inline def subtract(v0:VEC):VEC = {
    euclid.subtract(v0)
      recognize(this)
  }

  inline def recognize(v: Any):VEC = v.asInstanceOf[VEC]


  def magnitudeSquared:Double = euclid.normSquared
  def magnitude:Double = euclid.norm

  inline def normalize():VEC = {
    val m2:Double = euclid.norm
    if (m2 > 0.0) divide(Math.sqrt(m2))
    else throw VectorNormalizationException(this)
  }

  inline def +=(v0:VEC):VEC = add(v0)
  inline def -=(v0:VEC):VEC = subtract(v0)
  inline def *= (scalar: Double):VEC = scale(scalar)
  inline def /= (divisor: Double):VEC = divide(divisor)

}

trait DenseVectorData extends VectorData {
  override val dimension:Int = values.length

  override inline def component(i:Int):Double = {
    inDimensionOrThrowException(i)
    values(i)
  }

  override inline def component(i: Int, d:Double): Double = {
    inDimensionOrThrowException(i)
    values(i) = d
    d
  }
}

trait SparseVectorData extends VectorData {
  val indices: VectorIndices

  // binary search
  private def localIndex(target: Int): Int = {
    inDimensionOrThrowException(target)
    var left = 0
    var right = indices.length - 1
    while (left <= right) {
      val mid = (left + right) / 2
      if (indices(mid) < target) left = mid + 1
      else if (indices(mid) > target) right = mid - 1
      else return mid
    }
    -1
  }

  override def component(i: Int): Double = localIndex(i) match {
    case li:Int if li < 0 => 0.0
    case li:Int => values(li)
  }

  override inline def component(i: Int, d:Double): Double = localIndex(i) match {
    case li:Int if li < 0 => -1
    case li:Int => values(li) = d; d
  }

}

trait VectorData extends Euclidean {

  val values: VectorValues

  /**
   * For exotic vector formatting, provide lambdas for generating prefix, delimiter, and suffix.
   *
   * @param prefix maps a vector to a prefix.
   * @param delimiter maps vector element at index i to a delimiter.
   * @param suffix maps vector to a suffix.
   * @return
   */
  def dynamicCustomToString(
                            prefix: VectorData => String,
                            delimiter:Int => String,
                            suffix: VectorData => String,
                            sb:StringBuilder = new StringBuilder(),
                            numberFormatter:Double => String = (d:Double) => d.toString ):StringBuilder = {

    sb.append(prefix(this))
    for (i <- 0 until values.length - 1) {
      sb.append(numberFormatter(this.values(i)))
        .append(delimiter(i))
    }
    sb.append(numberFormatter(this.values(values.length - 1)))
      .append(suffix(this))
  }

  def customToString(
                prefix:String,
                delimiter:String,
                suffix:String,
                sb:StringBuilder = new StringBuilder(),
                numberFormatter:Double => String = (d:Double) => d.toString ):StringBuilder = {
    sb.append(prefix)
      .append(delimitedValues(delimiter, sb, numberFormatter))
      .append(suffix)
  }

  def commaSeparatedValues( sb:StringBuilder = new StringBuilder(),
                            numberFormatter:Double => String = (d:Double) => d.toString ):StringBuilder = delimitedValues(", ", sb, numberFormatter)

  def tabSeparatedValues( sb:StringBuilder = new StringBuilder(),
                          numberFormatter:Double => String = (d:Double) => d.toString ):StringBuilder = delimitedValues("\t", sb, numberFormatter)

  def delimitedValues( separator:String,
                       sb:StringBuilder = new StringBuilder(),
                       numberFormatter:Double => String = (d:Double) => d.toString ): StringBuilder = {
    sb.append(values.head)
    for (v <- values.tail) sb.append(separator).append(numberFormatter(v))
    sb
  }
}

case class UnsupportedVectorDimension(d:Int) extends Exception(
  s"Vector dimensions must exceed 1.  Cannot create a vector of dimension: $d"
)


case class VectorNormalizationException(v:VectorData) extends Exception(s"Can't normalize $v")