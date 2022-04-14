package ai.dragonfly.math

import ai.dragonfly.math.Constant.log2
import ai.dragonfly.math.vector.*

import java.math.MathContext

object Random {

  val defaultRandom:scala.util.Random = new scala.util.Random()

  extension(r:scala.util.Random)
  // Extension Methods for generating Random BigInt values
    private def bitCount(n: Int) = (n / log2).toInt
    private def digitCount(n: Int) = (n * log2).toInt
    def nextBigInt(precision: Int): BigInt = BigInt(bitCount(precision), r)
    def nextBigInt(norm: BigInt): BigInt = {
      val precision: Int = digitCount(norm.bitLength);
      (BigDecimal(norm)(new MathContext(precision + 1)) * BigDecimal(nextBigInt(precision))).toBigInt
    }
    def between(min: BigInt, MAX: BigInt): BigInt = min + r.nextBigInt(MAX - min)
    // Extension Methods for generating Random BigDecimal values
    def nextBigDecimal(precision: Int): BigDecimal = { val bd = BigDecimal( r.nextBigInt(precision) ); BigDecimal(bd.bigDecimal.movePointLeft(bd.precision)) }
    def nextBigDecimal(norm: BigDecimal, scale: Int): BigDecimal = norm * r.nextBigDecimal(scale)(new MathContext((norm.precision - norm.scale) + scale))
    def between(min: BigDecimal, MAX: BigDecimal): BigDecimal = min + r.nextBigDecimal(MAX - min, Math.max(min.precision, MAX.precision))
    def nextVector(dimension:Int, maxNorm:Double = 1.0): Vector[_] = Vector( VectorValues.tabulate(dimension)( (i:Int) => maxNorm * r.nextDouble() ) )
    def nextVector2(maxNorm:Double = 1.0):Vector2 = r.nextVector(2, maxNorm).asInstanceOf[Vector2]
    def nextVector3(maxNorm:Double = 1.0):Vector3 = r.nextVector(3, maxNorm).asInstanceOf[Vector3]
    def nextVector4(maxNorm:Double = 1.0):Vector4 = r.nextVector(4, maxNorm).asInstanceOf[Vector4]
    def nextVectorN(dimension:Int, maxNorm:Double = 1.0):VectorN = r.nextVector(dimension, maxNorm).asInstanceOf[VectorN]
    def next[V <: Vector[V]](norm:V):V = norm.recognize( Vector( VectorValues.tabulate(norm.dimension)((i:Int) => norm.values(i) * r.nextDouble() ) ) )
    def between[V <: Vector[V]](min:V, MAX:V):V = r.next[V]((MAX - min).add(min))

}
