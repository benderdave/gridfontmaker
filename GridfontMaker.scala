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
//    - either have configurable intro-letter/row spacing
//
//  - maybe save example text font-size/spacing/etc to .gf
//
//  - check all class imports and with GlobalFont
//  - general code review
//  - use adapters instead of listeners for brevity
//  - test on newer windows, newer mac
//  - slim down gf file by changing stroke to (Int,Int)
//
//  - JWrapper
//    - mounting mac installer doesn't automatically show drive in finder
//    - associate .gf with GridfontMaker (hard on os x)
//    - jwrapper is overriding: 
//        System.setProperty("apple.laf.useScreenMenuBar", "true")
//    - linux32: redo (copy letter) isn't showing up, but action happening
//
package farg

import scala.io.Source
import java.io.IOException
import javax.swing.UIManager
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

    val lcOSName = System.getProperty("os.name").toLowerCase()
    val isMac = lcOSName.startsWith("mac os x")
    if (isMac) {
      // NOTE: *both* of these must be done before the frame is created
      System.setProperty("apple.laf.useScreenMenuBar", "true")
      System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS")
    }

    gridfontFilename match {
      case "" => 
        val gfont = Font()
        GridfontMakerFrame.enable(gfont, "", isMac)
      case filename: String =>
        try {
          val encodedFontStr = Source.fromFile(filename).getLines.mkString
          val gfont = fromJson[Font](encodedFontStr)
          GridfontMakerFrame.enable(gfont, filename, isMac)
        } catch {
          case e: IOException => println(s"can't open file $filename")
        }
    }
  }
}
