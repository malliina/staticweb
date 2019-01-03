package mdoc

import java.nio.file.{Files, Paths}

import com.cibo.evilplot.geometry.Drawable

import scala.meta.inputs.Position

/**
  * @see https://github.com/scalameta/mdoc/blob/master/mdoc-docs/src/main/scala/mdoc/docs/EvilplotModifier.scala
  */
class MyPlotModifier extends PostModifier {
  override val name = "demoplot"

  override def process(ctx: PostModifierContext): String = {
    val relPath = Paths.get(ctx.info)
    val out = ctx.outputFile.toNIO.getParent.resolve(relPath)
    ctx.lastValue match {
      case d: Drawable =>
        Files.createDirectories(out.getParent)
        if (!Files.isRegularFile(out)) {
          d.write(out.toFile)
        }
        s"![](${ctx.info})"
      case _ =>
        val (pos, obtained) = ctx.variables.lastOption match {
          case Some(variable) =>
            val prettyObtained =
              s"${variable.staticType} = ${variable.runtimeValue}"
            (variable.pos, prettyObtained)
          case None =>
            (Position.Range(ctx.originalCode, 0, 0), "nothing")
        }
        ctx.reporter.error(
          pos,
          s"""type mismatch:
            expected: com.cibo.evilplot.geometry.Drawable
            obtained: $obtained"""
        )
        ""
    }
  }
}
