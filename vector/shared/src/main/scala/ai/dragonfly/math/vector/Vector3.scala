package ai.dragonfly.math.vector

import ai.dragonfly.math.example.Demonstrable
import ai.dragonfly.math.squareInPlace
import bridge.array.*

/**
 * Created by clifton on 1/10/17.
 */

object Vector3 extends VectorCompanion[Vector3] with Demonstrable {

  inline given dimension: Int = 3

  override inline def validDimension(dimension: Int): Boolean = dimension == 3

  override def apply(values:ARRAY[Double]): Vector3 = new Vector3(dimensionCheck(values, dimension))

  def apply(x: Double, y: Double, z: Double):Vector3 = apply(ARRAY[Double](x, y, z))

  def random(maxNorm:Double = 1.0): Vector3 = Vector3(maxNorm * Math.random(), maxNorm * Math.random(), maxNorm * Math.random())


  override def demo(implicit sb:StringBuilder = new StringBuilder()):StringBuilder = {
    val i = Vector3(1, 0, 0)
    val j = Vector3(0, 1, 0)
    val k = Vector3(0, 0, 1)

    sb.append(s"i3 X j3 -> ${i ⨯ j}\n")
    sb.append(s"j3 X i3 -> ${j ⨯ i}\n")

    sb.append(s"i3 X k3 -> ${i ⨯ k}\n")
    sb.append(s"k3 X i3 -> ${k ⨯ i}\n")

    sb.append(s"j3 X k3 -> ${j ⨯ k}\n")
    sb.append(s"k3 X j3 -> ${k ⨯ j}\n")

    sb.append(s"i3 dot j3 -> ${i dot j}\n")
    sb.append(s"j3 dot i3 -> ${j dot i}\n")

    sb.append(s"i3 dot k3 -> ${i dot k}\n")
    sb.append(s"k3 dot i3 -> ${k dot i}\n")

    sb.append(s"j3 dot k3 -> ${j dot k}\n")
    sb.append(s"k3 dot j3 -> ${k dot j}\n")
  }

  override def name: String = "Vector3"
}


case class Vector3 private (override val values:ARRAY[Double]) extends Vector {

  type VEC = Vector3

  inline def x:Double = values(0)
  inline def y:Double = values(1)
  inline def z:Double = values(2)

  inline def ⨯ (v: Vector3): Vector3 = cross(v)

  inline def cross(v: Vector3): Vector3 = Vector3(
    y * v.z - z * v.y, // u2*v3 - u3*v2,
    z * v.x - x * v.z, // u3*v1 - u1*v3,
    x * v.y - y * v.x  // u1*v2 - u2*v1
  )

  override inline def copy(): VEC = Vector3(x, y, z)

  override def toString: String = s"《³↗〉${x}ᵢ ${y}ⱼ ${z}ₖ〉"

}