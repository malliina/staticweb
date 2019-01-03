# staticweb

Here's a version from build.sbt:

```scala
libraryDependencies += "com" % "lib" % "@VERSION@"
```

```scala mdoc:silent
val x = 1
```

Here's the code:

```scala mdoc:code:code/CodeSamples.scala:example1
42
```

For details, see [details](details.md)

This is illegal:

```scala mdoc:fail
vay y = 1
```

This throws:

```scala mdoc:crash
val answer = 1/0
```

Plot:

```scala mdoc:demoplot:assets/scatterplot.png
import com.cibo.evilplot._
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.numeric.Point

val data = Seq.tabulate(90) { i =>
  val degree = i * 8
  val radian = math.toRadians(degree)
  Point(i.toDouble, math.sin(radian))
}

ScatterPlot(data)
  .xAxis()
  .yAxis()
  .frame()
  .xLabel("x")
  .yLabel("y")
  .render()
```
