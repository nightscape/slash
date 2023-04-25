# vector

A Scala 3 Vector Math and Statistics library designed to simultaneously provide convenient syntax, maximize performance, and cross compile to JVM, Native and Scala.js platforms.  <a href="https://dragonfly-ai.github.io/vector/">Try the demo</a>.

<h3>Vector Library Design 101 (what this library is not):</h3>

&nbsp;&nbsp;&nbsp;To understand how this library avoids tradeoffs that have plagued other vector math libraries since the advent of computers, please contrast the following three illustrations; each represents a competing family of vector type design priorities:
<table>
<tr>
<td style="text-align: center; width: 25%;">Case Classes (<a href="https://scastie.scala-lang.org/ClaUW7DmQOCtT2rlAhFEtg">Scastie</a>)</td>
<td style="text-align: center; width: 25%;">Wrappers and Traits (<a href="https://scastie.scala-lang.org/oH0TYuYERCa8w21NeKjwrw">Scastie</a>)</td>
<td style="text-align: center; width: 25%;">Arrays (<a href="https://scastie.scala-lang.org/VVTNglXrSrW8uDrd9iYmyg">Scastie</a>)</td>
</tr>

<tr style="vertical-align: top;">
<td style="width: 25%;">Strictly Immutable Version:

```scala
case class Vector2(x:Double, y:Double) {
  def + (that:Vector2):Vector2 = Vector2(
    x + that.x, y + that.y
  )
}

case class Vector3(x:Double, y:Double, z:Double) {
  def + (that:Vector3):Vector3 = Vector3(
    x + that.x, y + that.y, z + that.z
  )
}
```
Strictly Mutable Version:

```scala
case class Vector2(var x:Double, var y:Double) {
  def += (that:Vector2):Vector2 = {
    x += that.x; y += that.y
    this
  }
}

case class Vector3(var x:Double, var y:Double, var z:Double) {
  def += (that:Vector3):Vector3 = {
    x += that.x; y += that.y; z += that.z
    this
  }
}
```

Combined Version to Offer Immutable or Mutable Behavior:

```scala
case class Vector2(var x:Double, var y:Double) {
  def + (that:Vector2):Vector2 = Vector2(
    x + that.x, y + that.y
  )
  def += (that:Vector2):Vector2 = {
    x += that.x; y += that.y
    this
  }
}

case class Vector3(var x:Double, var y:Double, var z:Double) {
  def + (that:Vector3):Vector3 = Vector3(
    x + that.x, y + that.y, z + that.z
  )
  def += (that:Vector3):Vector3 = {
    x += that.x; y += that.y; z += that.z
    this
  }
}
```
</td>
<td style="width: 25%;">

```scala
def dimCheck(sup:Int, req: Int): Unit = {
  if(sup!=req) throw Exception("Mismatched Dimensions!")
}

trait Vector {
  type VEC <: Vector
  val dimension:Int

  def apply(i:Int):Double
  def update(i:Int, d:Double): Unit
  def zeros:VEC
  inline def +(that:VEC):VEC = {
    dimCheck(dimension, that.dimension)
    val out = zeros
    var i = 0; while(i < dimension) {
      out(i) = this(i) + that(i)
      i = i + 1
    }
    out
  }
}

object Vector2 {
  def apply(values:Array[Double]): Vector2 = {
    dimCheck(2, values.length); new Vector2(values)
  }
  def apply(x:Double, y:Double): Vector2 = {
    new Vector2(Array[Double](x, y))
  }
}

class Vector2 private (values:Array[Double]) extends Vector {
  type VEC = Vector2
  override val dimension:Int = 2
  inline def x:Double = values(0)
  inline def y:Double = values(1)
  inline override def apply(index: Int): Double = values(index)
  inline override def update(index: Int, value: Double): Unit = {
    values(index) = value
  }
  override def zeros:Vector2 = Vector2(0.0, 0.0)
  override def toString:String = s"Vector2($x, $y)"
}

object Vector3 {
  def apply(values:Array[Double]): Vector3 = {
    dimCheck(3, values.length); new Vector3(values)
  }
  def apply(x:Double, y:Double, z:Double): Vector3 = {
    new Vector3(Array[Double](x, y, z))
  }
}

class Vector3 private (values:Array[Double]) extends Vector {
  type VEC = Vector3
  override val dimension:Int = 3
  inline def x:Double = values(0)
  inline def y:Double = values(1)
  inline def z:Double = values(1)

  inline override def apply(index: Int): Double = values(index)
  inline override def update(index: Int, value: Double): Unit = {
    values(index) = value
  }

  override def zeros:Vector3 = Vector3(0.0, 0.0, 0.0)
  override def toString:String = s"Vector3($x, $y, $z)"
}
```
</td>
<td style="width: 25%;">

```scala
type Vector = Array[Double]

object Vector {
  def plus(v1:Vector, v2:Vector):Vector = {
    if (v1.length != v2.length) {
      throw Exception("Mismatched Vector Dimensions!")
    } else {
      val out: Vector = new Array[Double](v1.length)
      var i:Int = 0
      while (i < v1.length) {
        out(i) = v1(i) + v2(i)
        i += 1
      }
      out
    }
  }
}
```
</td>
</tr>

<tr>
<td style="vertical-align: top; width: 25%;">

Summary:<br />
&nbsp;&nbsp;&nbsp;Vectors very commonly appear in the form of case classes like these.  The Scala.js game engine: <a href="https://github.com/PurpleKingdomGames/indigo/blob/main/indigo/indigo/src/main/scala/indigo/shared/datatypes/Vector2.scala#L5">Indigo</a> has taken this approach very far, and the <a href="https://github.com/dragonfly-ai/vector/commit/6ea051e89f0272a099662d7a9d565a9bea77be23#diff-2b17728622fdb5f2e6eb99607354b2652b818bbe28bd39117f68f3bac034c9f0R10">earliest versions of this library</a> also adopted it.  Although more legible and intuitive than the alternatives, this approach lags farthest behind in performance: "That's a lot of memory overhead for what's essentially an ordered sequence of `Double` values.", flexibility: "How can case classes represent vectors of high dimension or dimensions determined at run time?", ease of maintenance: "Who wants to maintain separate implementations of `def + (v: Vector): Vector` for every possible vector dimension?", and portability: "Scala case classes don't readily serialize efficiently to JSON or Binary compared to other types, and appear less accessible to native Java, C, and JavaScript developers."<br />
</td>
<td style="vertical-align: top; width: 25%;">

Summary:<br />
&nbsp;&nbsp;&nbsp;To increase flexibility and reduce maintenance costs without addressing performance concerns, <a href="https://github.com/dragonfly-ai/vector/blob/c9e370545d96a8e341b63e1c4ee39be846b0f970/vector/shared/src/main/scala/ai/dragonfly/math/vector/Vector3.scala#L25">Versions of this library as recent as 2023</a> reflected this approach.  With inheritance, type tricks, and wrappers, this kind of design can generalize operations like: `magnitude`, `def plus(v:Vector):Vector`, and `def scale(s:Double):Vector` across all vector dimensions to consolidate most of the methods from each vector type into a single shared trait.  Unfortunately, the design gets complicated, introduces potential runtime errors, and doesn't improve performance.<br />
</td>

<td style="vertical-align: top; width: 25%;">

Summary:<br />
&nbsp;&nbsp;&nbsp;This approach puts performance first, absolutely minimizes memory footprint and maintenance costs, and unless it abandons runtime dimension checking, also maximizes speed.  Unfortunately, emphasizing performance to this extent invites innumerable runtime errors, and subjects users to unpleasant syntax. 
</td>
</tr>


<tr style="vertical-align: top;">
<td style="width: 25%;">
Advantages:
<ul>
<li>Intuitive syntax.

```scala
val v = Vector2(0.5, 0.25) + Vector2(0.5, 0.25)
```
</li>
<li>Type Safe.

```scala
// compiler error:
val v3 = Vector2(0.5, 0.25) + Vector3(0.5, 0.25, 0.125)
```
</li>
<li>Potentially Strictly Immutable.</li>
<li>

Human Readable `toString` => `Vector2(1.0,0.5)`.
</li>
<li>

Supports Overloaded Operators: `+, -, *, /, etc.`
</li>
<li>No runtime errors.</li>
<li>Dimensionality built into the type; no runtime dimension checking.</li>
<li>No loop overhead for + method.</li>
</ul>
Disadvantages:
<ul>
<li>No clear way to implement vectors of higher or runtime determined dimension.</li>
<li>Separate implementations for every vector dimensionality.</li>
<li>Memory intensive.</li>
<li>Slow.</li>
<li>

Not generic:  Suppose we need a `VectorBounds` class which can tell whether a vector lies within a rectangular volume or not.  Because `Vector2` and `Vector3` are entirely distinct types, we need separate implementations of `VectorBounds` for every possible dimensionality.
</li>
<li>

Tempts users into relying on `.equals` and `.hashcode`.  However, in practice, floating point errors make `Vector` vector data error prone when used as `Map` keys or in equality testing.  For example:
```scala
// Returns true only sometimes:
v2.equals( v2.rotate(Math.PI / 4.0).rotate(-Math.PI / 4.0) ) 
```
</li>
<li>Not portable.  Array[Double] has long served as the common currency between machine learning, statistics, matrix, and other math libraries on all of Scala's target platforms.  Case classes, by contrast, require conversions to make use of 3rd party libraries.</li>
<li>

Bloated default serializations.  Whether JSON, or binary, automatic serializations of case classes create more bloated formats than `Array[Double]`.  For example, we might prefer JSON in the format:<br />
`[1.0, 2.0, 3.0]` instead of `{ "x" : 1.0, "y" : 2.0, "z" : 3.0 }`.
</li>
</ul>
</td>

<td style="width: 25%;">

Advantages:
<ul>
<li>Intuitive syntax.

```scala
val v = Vector2(0.5, 0.25) + Vector2(0.5, 0.25)
```
</li>
<li>Type Safe.

```scala
// compiler error:
val v3 = Vector2(0.5, 0.25) + Vector3(0.5, 0.25, 0.125)
```
</li>
<li>Potentially Strictly Immutable.</li>
<li>

Allows overrides for `toString`.
</li>
<li>

Supports Overloaded Operators: `+, -, *, /, etc.`
</li>
<li>No runtime errors from operations involving multiple vectors of the same, explicitly defined, low dimensional type.</li>
<li>Generic.  Supports syntax like:

```scala
case class VectorBounds[V <: Vector](min:V, MAX:V) {
  def contains(v: V): Boolean = {
    var o: Boolean = true
    var i: Int = 0; while (o && i < min.dimension) {
      o = min.component(i) <= v.component(i) && v.component(i) <= MAX.component(i)
      i += 1
    }
    o
  }
}
```
</li>
<li>Shares common methods between vector implementations instead of forcing unique implementations for every possible dimensionality.</li>
<li>Only one implementation for every possible vector dimension.</li>
<li>Accommodates higher dimensional vectors, but without compile time dimensionality-linked type safety:

```scala
class VectorN private (override val values:Array[Double]) extends Vector { }
```
</li>
</ul>
Disadvantages:
<ul>
<li>Complicated!</li>
<li>Separate implementations for every vector dimensionality.</li>
<li>Slow: relies on traits and wrappers for convenient syntax at the expense of runtime performance.</li>
<li>Memory intensive: suffers from too many runtime dimension checks, wrapper instantiations, and trait abstraction overhead.</li>
<li></li>
<li>

Allows overrides for `hashCode` and `equals` which tempts developers into making bad business logic decisions.
</li>
<li>Runtime dimensionality checks on every operation involving two vectors.</li>
<li>Exposes some runtime errors when creating Vector types from arrays of incorrect length.</li>
<li>

Slightly less bloated default serializations.  For example, we might prefer JSON in the format:<br />
`[1.0, 2.0, 3.0]` instead of `{ "values" : [1.0, 2.0, 3.0] }`.
</li>
</ul>
</td>

<td style="width: 25%;">
Advantages:
<ul>
<li>Fast.</li>
<li>Minimal memory footprint.</li>
<li>Simplest code base to maintain.</li>
<li>

Optimally concise default serialization formats with JSON defaulting to:
`[1.0, 2.0, 3.0]`
</li>
</ul>
Disadvantages:
<ul>
<li>No vector dimensionality errors detected at compile time; they only appear as runtime errors.</li>
<li>Terrible Syntax:

```scala
val v1:Vector = Array[Double](1.0, 2.0)
val v2:Vector = Array[Double](3.0, 4.0, 5.0)
// runtime exception instead of compiler error
val v3:Vector = Vector.plus(v1, v2)
```
</li>
<li>Small performance penalty from requiring dimensionality checks on every operation involving two vectors.</li>
</ul>
</td>
</tr>
</table>

<h3>How This Vector Library Surpasses the Strengths of All Three:</h3>

&nbsp;&nbsp;&nbsp;What if we could have a Vector type with less overhead and more speed than the Arrays method, more flexibility than the Traits and Wrappers method, and more robust compile time error detection than the Case Class method?  What if we could also maximize portability to native compilation targets for seamless interop with native language families: JavaScript, Java, and C/C++, all in a way that serialization libraries inherently treat in the most concise possible way?  This library makes good use of Scala 3 features to meet all of these goals.  Please consider <a href="https://github.com/dragonfly-ai/vector/blob/cb962a3b9d154eea37ffec877b25fa256e374ba7/vector/shared/src/main/scala/ai/dragonfly/math/vector/package.scala#L27">the design</a>:

```scala
import narr.*
import scala.compiletime.ops.int.*

package object vector {

  // What if the dimension occupied the type parameter?
  // Now we can have the syntax of case class vectors on types that reduce to pure natively typed arrays at runtime!
  opaque type Vector[N <: Int] = NArray[Double] // NArray is a type alias for the best available native Array type.

  object Vector {
    // convenience methods for Vector[2], Vector[3], and Vector[4]
    inline def apply(x: Double, y: Double): Vector[2] = NArray[Double](x, y)
    inline def apply(x: Double, y: Double, z: Double): Vector[3] = NArray[Double](x, y, z)
    inline def apply(x: Double, y: Double, z: Double, w: Double): Vector[4] = NArray[Double](x, y, z, w)    
    
    // painless conversion from NArray[Double] to Vector[N]
    inline def apply[N <: Int](a: NArray[Double]): Vector[N] = { // sneaky way to cast an NArray[Double] to a Vector[N]
      dimensionCheck(a, valueOf[N])
      a
    }

    // We get convenient object oriented syntax through the use of extension methods:
    extension[N <: Int] (thisVector: Vector[N]) {
      // inline everywhere, for speed!
      inline def dimension: Int = thisVector.length
      inline def apply(index: Int): Double = thisVector(index)
      inline def update(index: Int, value: Double): Unit = thisVector(index) = value

      // immutable support:
      inline def +(v0: Vector[N]): Vector[N] = copy.add(v0)

      // mutable support for those who need speed more than safety:
      inline def += (v0: Vector[N]): Vector[N] = add(v0)

      // one add method for all vectors of all possible dimensions
      def add(v0: Vector[N]): Vector[N] = {
        var i = 0
        while (i < dimension) {
          thisVector(i) = thisVector(i) + v0(i)
          i = i + 1
        }
        thisVector
      }
    }
}
```
Please consider also, the flexibility to enable types that use Vectors of varying sizes: 
```scala
package ai.dragonfly.math.vector
import Vector.*

// instead of putting the Vector type in the type parameter, just place the vector dimensionality.
case class VectorBounds[N <: Int](min: Vector[N], MAX: Vector[N]) {
  def contains(v: Vector[N]):Boolean = {
    var o:Boolean = true
    var i:Int = 0; while(o && i < min.dimension) {
      o = min(i) <= v(i) && v(i) <= MAX(i)
      i += 1
    }
    o
  }
}
```

<h3>More Examples of this Sweet Vector Math Syntax:</h3>

```scala
import ai.dragonfly.math.vector.*
import ai.dragonfly.math.vector.Vector.*

// create a 3 dimensional vector
val v1:Vector[3] = Vector[3](1.0, 0.5, 0.0)

// print it to the console
println(v1.show)  // -> 《³↗〉1.0ᵢ 0.0ⱼ 0.0ₖ〉

// perform various vector math operations
val v2:Vector[3] = Vector[3](0.75, 1.0, 0.5)

val v3:Vector[3] = v1 + v2

println( v3.norm )

v3 -= v1

println( v3.euclideanDistanceTo(v2) )

// perform Vector3 specific operations
import ai.dragonfly.math.vector.Vector3.*

println(v1.x) // .x .y and .z provided by an extension method in Vector3

// compute 3D Vector Cross product
println( (v1 cross v2 ).show ) 
println( (v1 ⨯ v2 ).show ) // or with a unicode ⨯ operator

import ai.dragonfly.math.Random.*
val r:Random = defaultRandom

// higher dimensional vectors
val v42a:Vector[42] = r.nextVector[42]()
val v42b:Vector[42] = r.nextVector[42]()

println( v42a dot v42b )
println( (v42a - v42b).render() )
println( (v42a + v42b).csv() ) // output vector sum as comma separated values
println( (v42a + v42b).tsv() ) // output vector sum tab separated values
```

<h3>NArray and JavaScript Optimization</h3>

Because vector relies on <a href="https://github.com/dragonfly-ai/narr">NArr</a>, JavaScript environments store vector data as: `Float64Array` while JVM and Native environments rely on: `Array[Double]`.  This ensures that whichever compilation target you choose, Vector will always reduce to the native array type with the highest available performance.


<h3>How to use it:</h3>
Installation with SBT:

```scala
libraryDependencies += "ai.dragonfly" %%% "vector" % "<LATEST_VERSION>"
```

Other Features:
- High performance, low memory footprint Vector type `Vector[N <: Int]` which consists of nothing more than `Float64Array` in JavaScript environments and `Array[Double]` on JVM and Native platforms.  Extension methods, Opaque Types, and Dependent Types combine to provide convenient syntax for developers without introducing any runtime overhead.
- Customisable Vector data to text converters.
- Parametric and Estimated (Online/Streaming) Probability Distributions: Gaussian/Normal, Poisson, LogNormal, Binomial (parametric only), Beta, and PERT; each with support for sampling and probability density functions, PDFs.
- Math functions: Beta, Factorial, and Gamma functions: B(α, β), x! and Γ(x).
- Geometry: Sample points uniformly from volumes defined by 3D tetrahedrons.  Bresenham Line Drawing Algorithm that invokes a lambda for each discrete point on a line.
- Kernels: Gaussian, Epanechnikov, Uniform, and Discrete.
- Flexible Histogram data structures with Console friendly Text Based Visualizations inspired by <a href="https://github.com/JuliaPlots/UnicodePlots.jl">Julia Plots</a>.
- Bijection[A, B]: an abstraction for bijective implicit conversions.
- BigRandom: scala.util.Random extension methods to generate random BigInt and BigDecimal values.
- Interval and Domain types and objects with support for random sampling.
- Unicode text formatting utility for writing numeric value types in superscript or subscript positions.


Unicode Histogram Plot:

```
Histogram: { 
	[-17.00,-11.00 ) 🌑 ︙    ∝ 3.0E-4
	[-11.00, -5.00 ) 🌑 ▕█   ∝ 0.0094
	[ -5.00,  1.00 ) 🌑 ▕████████    ∝ 0.0732
	[  1.00,  7.00 ) 🌒 ▕███████████████████████████    ∝ 0.2447
	[  7.00, 13.00 ) 🌓 ▕███████████████████████████████████████   ∝ 0.3516
	[ 13.00, 19.00 ) 🌔 ▕██████████████████████████▋   ∝ 0.2411`
	[ 19.00, 25.00 ) 🌔 ▕███████▌   ∝ 0.069
	[ 25.00, 31.00 ) 🌔 ▕█   ∝ 0.0101
	[ 31.00, 37.00 ) 🌔 ︙    ∝ 5.0E-4
	[ 37.00, 43.00 ] 🌕 ︰    ∝ 1.0E-4
}
```
These plots label the bins with standardised math notation for open and closed intervals, but also use the unicode glyphs for the phases of the moon to represent the cumulative distribution.

<h2>FAQ:</h2>
<ul>
<li>

`NArray[Double]` is useful, but why exclude `NArray[Float]`?

In Scala.js, the runtime type of `Float` doesn't fully exist, while Scala.js, Scala JVM, and Scala Native all share the same implementation of `Double`.
</li>
</ul>
<br />

Projects that rely on this Library:

https://github.com/dragonfly-ai/matrix

https://github.com/dragonfly-ai/bitfrost

https://github.com/dragonfly-ai/spatial

https://github.com/dragonfly-ai/graphics

https://github.com/dragonfly-ai/img

Acknowledgements:
This library has evolved over years, but owes a lot of its virtues to feedback from the Scala Discord community.  In no particular order:

https://github.com/ekrich

https://github.com/armanbilge

https://github.com/s5bug

https://github.com/BalmungSan