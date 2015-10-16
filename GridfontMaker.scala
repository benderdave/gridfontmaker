// TODO
//  - bug: initial size isn't correct (showing central zone reveals this)
//
//  - EditPanel
//    - allow drag to locations *before* first letter and *after* last one
//
//  - EditableLetter
//    - ability to override horizontal extent (offet and width)
//    - maybe animate history for each letter (no)
//
//  - GridfontTextArea
//    - add highlight/cut/paste
//    - either have configurable intro-letter/row spacing or tighten
//
//  - maybe save example text font-size/spacing/etc to .gf
//
//  - check all class imports and with GlobalFont
//  - general code review
//  - test on newer windows, newer mac
//  - slim down gf file by changing stroke to (Int,Int)
// *- figure out how to associate file with .gf so (at least on a mac)
//     double-clicking opens font in the editor
//  - add close button to font compare
//
//  - JWrapper
//    - mounting mac installer doesn't automatically show drive in finder
//    - quitting mac version (ctrl-q) doesn't close app
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
