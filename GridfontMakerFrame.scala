package farg

import java.io.File
import scala.io.Source
import com.owlike.genson.defaultGenson._
import java.awt.{Toolkit, Dimension, Color, BorderLayout, BasicStroke}
import java.awt.event.{WindowListener, WindowEvent}
import java.awt.BasicStroke._
import javax.swing.{JFrame, SwingUtilities, JFileChooser, JOptionPane}
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import javax.swing.filechooser.{FileFilter, FileNameExtensionFilter}
import java.util.{Observer, Observable}

// ----------------------------------------------------------------------------
trait GlobalFont {
  def gfont: Font = GridfontMakerFrame.gfont
}

// ----------------------------------------------------------------------------
trait PlatformSpecificInit {
  def initialize(frame: JFrame): Unit
}

class GridfontMakerFrame(var filename: String, isMac: Boolean) extends JFrame 
    with WindowListener with GlobalFont {
  setBackground(Color.black)
  val alphAndNameArea = new AlphabetAndNameArea
  val textPanel = new ExampleTextPanel(18.0, gfont.example_text)
  val editPanel = new EditPanel(
    Seq(alphAndNameArea.alphPanel.textArea, textPanel.textArea)
  )
  add(alphAndNameArea, BorderLayout.NORTH)
  add(editPanel, BorderLayout.CENTER)
  add(textPanel, BorderLayout.SOUTH)
  addWindowListener(this)
  val gridfontMenuBar = new GridfontMenuBar(this, isMac)
  setJMenuBar(gridfontMenuBar)

  var actionStack: ActionStack = 
    new ActionStack(gridfontMenuBar.undo, gridfontMenuBar.redo)

  var savedFontState = toJson(gfont)

  var gfDirectory = new File(".")
  val gfFilter = new FileNameExtensionFilter("Gridfont files", "gf")

  if (isMac) macSpecificSetup

  def hasChanges: Boolean = {
    val currentState = toJson(gfont)
    savedFontState != currentState
  }

  def getBaseExt(fname: String): (String, String) = {
    val baseExt = fname.split('.')
    if (baseExt.length > 1) {
      val base = baseExt.slice(0, baseExt.length-1).mkString(".")
      val ext = baseExt.last
      (base, ext)
    } else {
      val base = fname
      val ext = "gf"
      (base, ext)
    }
  }
    
  override def windowClosing(e: WindowEvent): Unit = {
    if (hasChanges) {
      JOptionPane.showConfirmDialog(this,
        "Save changes?", "Save on Exit",
        JOptionPane.YES_NO_OPTION) match {
        case 0 => saveFile
        case _ =>
      }
    }
    // NOTE: must explicitly exit when running under JWrapper on OSX -- lame
    System.exit(0)
  }
  override def windowActivated(e: WindowEvent): Unit = {}
  override def windowClosed(e: WindowEvent): Unit = {}
  override def windowDeactivated(e: WindowEvent): Unit = {}
  override def windowIconified(e: WindowEvent): Unit = {}
  override def windowDeiconified(e: WindowEvent): Unit = {}
  override def windowOpened(e: WindowEvent): Unit = {}

  def getSaveName: String = {
    val (base, ext) = getBaseExt(filename)
    if (base != "") s"${base}.${ext}"
    else ""
  }

  def saveFile: Unit = {
    if (hasChanges) {
      val saveName = getSaveName
      if (saveName == "") saveFileAs
      else savedFontState = gfont.save(saveName)
    } else {
      println("no need to save")
    }
  }

  def saveFileAs: Unit = {
    val savechooser = new JFileChooser(gfDirectory)
    savechooser.setFileFilter(gfFilter)
    val returnVal = savechooser.showSaveDialog(this)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      gfDirectory = savechooser.getCurrentDirectory()
      val file = savechooser.getSelectedFile()
      val newFilename = file.getCanonicalFile.toString()
      val newFilenameWithExt = if (newFilename.endsWith(".gf")) newFilename
                               else newFilename + ".gf"
      filename = newFilenameWithExt
      savedFontState = gfont.save(filename)
    }
  }

  def newGrid: Unit = {
    alphAndNameArea.clear
    textPanel.textArea.clear
    editPanel.clearAll()
    filename = ""
    savedFontState = toJson(gfont)
    actionStack.reset
    repaint()
  }

  def openFile: Unit = {
    val openchooser = new JFileChooser(gfDirectory)
    openchooser.setFileFilter(gfFilter)
    val returnVal = openchooser.showOpenDialog(this)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      gfDirectory = openchooser.getCurrentDirectory()
      val file = openchooser.getSelectedFile()
      val newFilename = file.toString
      filename = newFilename
      val encodedFontStr = Source.fromFile(filename).getLines.mkString
      try {
        val gfont = fromJson[Font](encodedFontStr)
        GridfontMakerFrame.gfont = gfont
        alphAndNameArea.namePanel.setFontName(gfont.name, true)
        alphAndNameArea.alphPanel.textArea.setText(('a' to 'z').mkString)
        savedFontState = toJson(gfont)
        textPanel.textArea.setText(gfont.example_text, true)
        actionStack.reset
        repaint()
      } catch {
        case e: Exception => {
          println(e)
          JOptionPane.showMessageDialog(this,
            s"There was a problem loading $filename", "Open file failed",
            JOptionPane.ERROR_MESSAGE)
        }
      }
    }
  }

  def addFontCompare: Unit = {
    val openchooser = new JFileChooser(gfDirectory)
    openchooser.setFileFilter(gfFilter)
    val returnVal = openchooser.showOpenDialog(this)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      gfDirectory = openchooser.getCurrentDirectory()
      val file = openchooser.getSelectedFile()
      val newFilename = file.toString
      val encodedFontStr = Source.fromFile(newFilename).getLines.mkString
      try {
        val compareFont = fromJson[Font](encodedFontStr)
        alphAndNameArea.addCompare(compareFont)
        repaint()
      } catch {
        case e: Exception => {
          println(e)
          JOptionPane.showMessageDialog(this,
            s"There was a problem loading $filename", "Open compare file failed",
            JOptionPane.ERROR_MESSAGE)
        }
      }
    }
  }

  def macSpecificSetup: Unit = {
    // NOTE: dynamically load so we don't try to find mac-specific classes on
    //   other platforms (e.g. AboutHandler)
    Class.forName("farg.MacOSXInit").getConstructor().newInstance()
      .asInstanceOf[PlatformSpecificInit].initialize(this)
  }
}

object GridfontMakerFrame {
  val lineStroke = new BasicStroke(4.0f, CAP_ROUND, JOIN_BEVEL)
  val largeLineStroke = new BasicStroke(5.5f, CAP_ROUND, JOIN_BEVEL)
  val smallLineStroke = new BasicStroke(2.3f, CAP_ROUND, JOIN_BEVEL)

  var gfont: Font = null
  var gui: GridfontMakerFrame = null

  def enable(gfont: Font, filename: String, isMac: Boolean): Unit = {
    GridfontMakerFrame.gfont = gfont
    val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
    val screenFraction = 0.9
    val preferredSize = new Dimension(
      (screenSize.getWidth * screenFraction).toInt,
      (screenSize.getHeight * screenFraction).toInt
    )
    gui = new GridfontMakerFrame(filename, isMac)
    gui.setDefaultCloseOperation(DISPOSE_ON_CLOSE)
    gui.setPreferredSize(preferredSize)
    gui.validate

    SwingUtilities.invokeAndWait(new Runnable() {
      override def run(): Unit = {
        gui.pack
        gui.setVisible(true)
      }
    })
  }
}
