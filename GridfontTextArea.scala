package farg

import java.awt.{Dimension, Color, Graphics2D, Graphics}
import java.awt.event.{MouseMotionListener, MouseEvent, MouseListener,
 KeyListener, KeyEvent, FocusListener, FocusEvent}
import java.awt.geom.Line2D
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import java.util.{Observer, Observable}
import java.awt.RenderingHints._

// ----------------------------------------------------------------------------
class ObservableText extends Observable {
  def changed(text: String): Unit = {
    setChanged
    notifyObservers(text)
  }
}

class ExampleTextPanel(initialSize: Double = 16.0, val initialText: String)
    extends JPanel {
  setBackground(Color.gray)
  val textArea = new GridfontTextArea(initialSize, initialText, true, true)
  setBorder(new EmptyBorder(10, 10, 10, 10))
  add(textArea)
}

class GridfontTextArea(initialSize: Double = 16.0, val initialText: String = "",
    var editable: Boolean = false, centered: Boolean = false) extends JPanel 
    with Observer with MouseMotionListener with MouseListener with KeyListener 
    with FocusListener with GlobalActionStack {
  import GridfontMaker._
  import GridfontMakerFrame._

  val obs = new ObservableText

  setBackground(Color.gray)
  addMouseMotionListener(this)
  addMouseListener(this)
  addKeyListener(this)
  addFocusListener(this)
  setFocusable(true)

  var gfontSize = 0.0
  var letterWidth = 0
  var letterHeight = 0
  var text = ""
  var lines: Array[String] = Array.empty
  var lineLengths: Array[Int] = Array.empty
  var maxLineLen = 0

  var carrotRow = 0
  var carrotCol = 0

  var hspacing = 1
  var vspacing = -4

  setSize(initialSize)
  setText(initialText, true)

  def clear: Unit = setText(initialText, true)

  def backspace: Unit = {
    if (carrotCol != 0 || carrotRow != 0) {
      if (carrotCol > 0) {
        carrotCol -= 1
        val newLines = for (i <- 0 until lines.length) yield {
          val line = lines(i)
          if (carrotRow == i) line.slice(0, carrotCol) +
            line.slice(carrotCol+1, line.length)
          else line
        }
        setText(newLines.mkString("\n"))
      } else {
        setText(lines.slice(0, lines.length-1).mkString("\n"))
        carrotRow -= 1
        carrotCol = lineLengths(lineLengths.length-1)
      }
    }
  }

  def insert(c: Char): Unit = {
    val newLines = for (i <- 0 until lines.length) yield {
      val line = lines(i)
      if (carrotRow == i) line.slice(0, carrotCol) + c.toString +
        line.slice(carrotCol, line.length)
      else line
    }
    if (c == '\n') {
      carrotCol = 0
      carrotRow += 1
    } else
      carrotCol += 1
    setText(newLines.mkString("\n"))
  }

  def left: Unit = {
    if (carrotCol == 0) {
      if (carrotRow > 0) {
        carrotRow -= 1
        carrotCol = lineLengths(carrotRow)
        repaint()
      }
    } else {
      carrotCol -= 1
      repaint()
    }
  }

  def right: Unit = {
    if (carrotCol < lineLengths(carrotRow)) {
      carrotCol += 1
      repaint()
    } else {
      if (carrotRow < lines.length-1) {
        carrotRow += 1
        carrotCol = 0
        repaint()
      }
    }
  }

  def up: Unit = {
    if (carrotRow > 0) {
      carrotRow -= 1
      carrotCol = Math.min(lineLengths(carrotRow), carrotCol)
      repaint()
    }
  }

  def down: Unit = {
    if (carrotRow < lines.length-1) {
      carrotRow += 1
      carrotCol = Math.min(lineLengths(carrotRow), carrotCol)
      repaint()
    }
  }

  override def keyPressed(e: KeyEvent): Unit = {
    if (!editable) return
    val k = e.getKeyCode
    if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_KP_LEFT)
      left
    else if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_KP_RIGHT)
      right
    else if (k == KeyEvent.VK_UP || k == KeyEvent.VK_KP_UP)
      up
    else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_KP_DOWN)
      down
  }

  override def keyReleased(e: KeyEvent): Unit = {}

  override def keyTyped(e: KeyEvent): Unit = {
    val c = e.getKeyChar.toLower
    if (editable &&
      (e.getExtendedKeyCode == KeyEvent.VK_BACK_SPACE) ||
      (e.getKeyChar == KeyEvent.VK_BACK_SPACE))
      backspace
    else if (editable && (c.isLetter || c == ' ' || c == '\n'))
      insert(c)
    else if (c == '+') {
      val newSize = Math.min(60.0, gfontSize + 1.0)
      actions.add(SizeChangeAction(this, () => {
        setSize(newSize)
        calculatePreferredSize
        repaint()
        getParent().revalidate
      }))
    } else if (c == '-') {
      val newSize = Math.max(4.0, gfontSize - 1.0)
      actions.add(SizeChangeAction(this, () => {
        setSize(newSize)
        calculatePreferredSize
        repaint()
        getParent().revalidate
      }))
    }
  }

  override def focusGained(e: FocusEvent): Unit = {}
  override def focusLost(e: FocusEvent): Unit = repaint()

  def calculatePreferredSize: Unit = {
    val width = letterWidth*maxLineLen + (maxLineLen-1)*hspacing
    val height = letterHeight*lines.length + (lines.length-1)*vspacing
    setPreferredSize(new Dimension(width, height))
  }

  def setSize(newSize: Double): Unit = {
    gfontSize = newSize
    letterWidth = (gfontSize*2).toInt
    letterHeight = (letterWidth/AspectRatio).toInt
  }

  def rowWidth(row: Int): Double = (letterWidth+hspacing)*lineLengths(row)

  def getCharPos(col: Int, row: Int): (Int, Int) = {
    val x = (letterWidth+hspacing)*col
    val y = (letterHeight+vspacing)*row
    if (centered)
      ((x + (getWidth - rowWidth(row))/2).toInt, y)
    else
      (x, y)
  }

  def getCarrotRowCol(x: Int, y: Int): (Int, Int) = {
    val row = Math.max(0, Math.min(lines.length-1,
      (y/(letterHeight+vspacing)).toInt))
    val cx = if (centered) (x + (rowWidth(row) - getWidth)/2) else x
    val col = ((cx+(letterWidth/2))/(letterWidth+hspacing)).toInt
    (row, Math.max(0, Math.min(col, lineLengths(row))))
  }

  def getAnchorPos(index: Int): (Int,Int) = {
    val rowStep = letterHeight / NumRows
    val rowPad = rowStep/2
    val colStep = letterWidth / NumCols
    val colPad = colStep/2
    val (row, col) = getRowCol(index)
    (colPad+col*colStep, rowPad+row*rowStep)
  }

  override def mouseDragged(e: MouseEvent): Unit = {
    if (editable) {
      val (row, col) = getCarrotRowCol(e.getX, e.getY)
      carrotRow = row
      carrotCol = col
      repaint()
    }
  }
  override def mouseMoved(e: MouseEvent): Unit = {}
  override def mouseEntered(e: MouseEvent): Unit = {}
  override def mouseExited(e: MouseEvent): Unit = {}

  override def mouseClicked(e: MouseEvent): Unit = mouseDragged(e)
  override def mousePressed(e: MouseEvent): Unit = requestFocus()
  override def mouseReleased(e: MouseEvent): Unit = {}

  def setText(newText: String, noChange: Boolean = false): Unit = {
    val textChange = ExampleTextChangeAction(() => {
      text = newText
      lines = newText.split("\n", newText.length)
      lineLengths = lines.map(_.length)
      maxLineLen = lineLengths.max
      calculatePreferredSize
      repaint()
      if (getParent() != null)
        getParent().revalidate()
      obs.changed(text)
    })
    if (noChange)
      textChange.doit(true)
    else
      actions.add(textChange)
  }

  def drawLetter(g2: Graphics2D, char: String, x: Int, y: Int): Unit = {
    val letter = gfont.letters(char)
    for ((start, end) <- letter.getStrokes) {
      val (startx, starty) = getAnchorPos(start)
      val (endx, endy) = getAnchorPos(end)
      val strokeLine = new Line2D.Double(startx+x, starty+y, endx+x, endy+y)
      g2.draw(strokeLine)
    }
  }

  def renderText(g2: Graphics2D): Unit = {
    g2.setColor(Color.white)
    g2.setStroke(smallLineStroke)
    for (i <- 0 until lines.length) {
      val row = lines(i)
      for (j <- 0 until row.length) {
        val char = row(j)
        val (x,y) = getCharPos(j, i)
        if (char != ' ') drawLetter(g2, char.toString, x, y)
      }
    }
  }

  def drawCarrot(g2: Graphics2D): Unit = {
    val (x,y) = getCharPos(carrotCol, carrotRow)
    g2.setColor(Color.black)
    g2.draw(new Line2D.Double(x, y, x, y+letterHeight))
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val g2 = g.asInstanceOf[Graphics2D]
    g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
    g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    renderText(g2)
    if (editable && hasFocus) drawCarrot(g2)
  }

  override def update(o: Observable, arg: Any): Unit = repaint()
}
