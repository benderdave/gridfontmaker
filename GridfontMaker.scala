// TODO
//  - bug: alph display sometimes doesn't resize correctly
//  - bug: close-all-others hoses alphabet display
//  - bug: sometimes a click (to add a single stroke) doesn't work. Maybe it's 
//         because there's a slight drag? I don't know. [Yup. So we just get
//         the mousePressed/mouseReleased events, and *not* mouseClicked. In
//         order to fix this we'd have to add support for adding/deleting
//         strokes both on a mouse-clicked and on mousePressed/Released events
//         where the same stroke has been selected the entire time (or there's
//         just been a small movement (which is better?).]
//  - bug: some actions can't be undone (just haven't added them yet)
//  - bug: in rare cases drag (and probably shift-drag) can get stuck, missing
//          the mouse-release.
//
//  - EditPanel
//    - allow drag to locations *before* first letter and *after* last one
//    - better animation of dragging a letter (copy and move); animate sort
//    - add drag instructions on mouse-over bottom area
//
//  - EditableLetter
//    - have to have the ability to manually override horizontal offet and 
//      width (float).
//
//  - GridfontTextArea
//    - add highlight, cut, paste, move-by-word, etc.
//    - add configurable inter-letter/row spacing
//
//  - general code review/refactor
//    - EditPanel.scala
//    - EditableLetter.scala
//    - GridfontMakerFrame.scala
//    - GridfontTextArea.scala
//
//  - check all class imports and with GlobalFont
//  - use adapters instead of listeners for brevity
//  - test on newer windows, newer mac
//  - maybe save example text font-size/spacing/etc to .gf
//  - figure out a way to view the name of the font using the font
//
//  - JWrapper
//    - mounting mac installer doesn't automatically show drive in finder
//    - associate .gf with GridfontMaker (not trivial on os x)
//    - jwrapper is overriding: 
//        System.setProperty("apple.laf.useScreenMenuBar", "true")
//    - linux32: redo (copy letter) isn't showing up, but action happening
//
package farg

import scala.io.Source
import java.io.IOException
import javax.swing.UIManager
import com.owlike.genson.defaultGenson._
import com.owlike.genson.JsonBindingException

// ----------------------------------------------------------------------------
object GridfontMaker {
  val NumCols = 3
  val NumRows = 7
  val NumAnchors = NumCols*NumRows
  val AspectRatio = NumCols.toDouble / NumRows.toDouble
  def getRowCol(index: Int) = (index/NumCols, index%NumCols)
  def rowColToIndex(row: Int, col: Int) = row*NumCols + col
  val fullAlphabet = ('a' to 'z')
  val fullAlphabetString = fullAlphabet.mkString

  var gridfontFilename: String = ""

  val commandLineParser = CommandLineParser(
    OptionalArg("-file", "", (filename: String) =>
      gridfontFilename = filename
    )
  )

  def main(args: Array[String]): Unit = {
    try {
      commandLineParser.parse(args.toList)
    } catch {
      case _: CommandLineArgError => System.exit(-1)
    }

    val lowercaseOSName = System.getProperty("os.name").toLowerCase()
    val isMac = lowercaseOSName.startsWith("mac os x")
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
          case e: JsonBindingException => println(s"unable to load $filename")
        }
    }
  }
}
