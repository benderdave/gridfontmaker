// TODO
//  - EditableLetter
//    - 'r' to rotate by 180 degrees around center
//   *- 1-6 to select horizontal extent of letter
//    - maybe animate history for each letter
//
//  - Grid
//    - optional three column layout option
//
//  - GridfontTextArea
//   *- support for per-letter width. For example, a narrow i or j should
//      display closer to the next letter
//    - fix up/down for centered text
//    - add highlight/cut/paste
//   *- either have a configurable stroke width or automatically change it
//      based on size
//   *- either have configurable intro-letter/row spacing or tighten
//
//  - maybe save example text font-size to .gf
//
//  - check all class imports and with GlobalFont
//  - general code review
//
//  - test on newer windows, newer mac
//
package farg

import scala.io.Source
import java.io.IOException
import com.owlike.genson.defaultGenson._

// ----------------------------------------------------------------------------
object GridfontMaker {
  val NumCols = 3
  val NumRows = 7
  val NumAnchors = NumCols*NumRows
  val AspectRatio = NumCols.toDouble / NumRows.toDouble
  def getRowCol(index: Int) = (index/NumCols, index%NumCols)
  def rowColToIndex(row: Int, col: Int) = row*NumCols + col

  var gridfontFilename: String = ""

  val commandLineParser = CommandLineParser(
    OptionalArg("-file", "", (filename: String) =>
      gridfontFilename = filename
    )
  )

  def main(args: Array[String]): Unit = {
    commandLineParser.parse(args.toList)

    gridfontFilename match {
      case "" => 
        val gfont = Font()
        GridfontMakerFrame.enable(gfont, "")
      case filename: String =>
        try {
          val encodedFontStr = Source.fromFile(filename).getLines.mkString
          val gfont = fromJson[Font](encodedFontStr)
          GridfontMakerFrame.enable(gfont, filename)
        } catch {
          case e: IOException => println(s"can't open file $filename")
        }
    }
  }
}
