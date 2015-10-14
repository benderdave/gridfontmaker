package farg

import java.awt.{Dimension, Color, Graphics2D, Graphics, BasicStroke}
import java.awt.event.{MouseMotionListener, MouseEvent, MouseListener,
 KeyListener, KeyEvent, FocusListener, FocusEvent}
import java.awt.geom.Line2D
import java.awt.BasicStroke._
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
  textArea.obs.addObserver(new Observer with GlobalFont {
    override def update(o: Observable, text: Any): Unit = 
      gfont.example_text = text.asInstanceOf[String]
  })
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
  var quantaWidth = 0.0
  var letterHeight = 0
  var text = ""
  var lines: Array[String] = Array.empty
  var lineLengths: Array[Int] = Array.empty
  var lineQuantaOffsets: Array[Array[Int]] = Array.empty
  var lineQuantaLengths: Array[Int] = Array.empty
  var maxLineLen = 0

  var lineStroke = new BasicStroke(2.3f, CAP_ROUND, JOIN_BEVEL)

  var carrotRow = 0
  var carrotCol = 0

  var hspacing = -1
  var vspacing = -7

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
        val beforeRows = lines.slice(0, carrotRow)
        carrotCol = lineLengths(carrotRow-1)
        beforeRows(beforeRows.length-1) += lines(carrotRow)
        val afterRows = lines.slice(carrotRow+1, lines.length)
        setText((beforeRows ++ afterRows).mkString("\n"))
        carrotRow -= 1
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
      val (x,y) = getMidCharPos(carrotCol, carrotRow)
      val (row,col) = getCarrotRowCol(x, ((y+letterHeight/2)-(letterHeight+vspacing/2)).toInt)
      carrotRow = row
      carrotCol = col
      repaint()
    }
  }

  def down: Unit = {
    if (carrotRow < lines.length-1) {
      val (x,y) = getMidCharPos(carrotCol, carrotRow)
      val (row,col) = getCarrotRowCol(x, ((y+letterHeight/2)+(letterHeight+vspacing/2)).toInt)
      carrotRow = row
      carrotCol = col
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
      val newSize = Math.min(80.0, gfontSize + 1.0)
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
    val width = (quantaWidth*lineQuantaLengths.max).toInt + (maxLineLen-1)*hspacing
    val height = letterHeight*lines.length + (lines.length-1)*vspacing
    setPreferredSize(new Dimension(width, height))
  }

  def setSize(newSize: Double): Unit = {
    gfontSize = newSize
    letterWidth = (gfontSize*2).toInt
    quantaWidth = letterWidth / NumCols.toDouble
    letterHeight = (letterWidth/AspectRatio).toInt
    lineStroke = new BasicStroke((newSize/5.2).toFloat, CAP_ROUND, JOIN_BEVEL)
  }

  def rowWidth(row: Int): Double = 
    (hspacing*(lineLengths(row)-1)) + (quantaWidth*lineQuantaLengths(row))

  def getCharPos(col: Int, row: Int): (Int, Int) = {
    val x = ((hspacing*col)+(quantaWidth*lineQuantaOffsets(row)(col))).toInt
    val y = (letterHeight+vspacing)*row
    if (centered)
      ((x + (getWidth - rowWidth(row))/2).toInt, y)
    else
      (x, y)
  }

  def getMidCharPos(col: Int, row: Int): (Int, Int) = {
    val (x,y) = getCharPos(col, row)
    val halfLetterWid = (getLetterWidthInQuanta(row, col)/2.0).toInt
    (x+halfLetterWid, y)
  }

  def getLetterWidthInQuanta(row: Int, col: Int): Int = {
    if (col < lineLengths(row)) {
      val ch = lines(row)(col)
      if (ch == ' ') spaceQuantaWidth
      else gfont.letters(ch.toString).width
    } else 0
  }

  def getCarrotRowCol(x: Int, y: Int): (Int, Int) = {
    val row = Math.max(0, Math.min(lines.length-1,
      (y/(letterHeight+(vspacing/2))).toInt))
    val cx = if (centered) (x + (rowWidth(row) - getWidth)/2) else x
    val col = lineQuantaOffsets(row).view.zipWithIndex.indexWhere { 
      case (qoff, idx) =>
        val halfLetterWid = getLetterWidthInQuanta(row, idx)/2.0
        (hspacing*idx)+(quantaWidth*(qoff+halfLetterWid)) >= cx
    }
    val finalCol = 
      if (col == -1) lineLengths(row)
      else Math.max(0, Math.min(col, lineLengths(row)))
    (row, finalCol)
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

  def spaceQuantaWidth = NumCols-1

  def getCharacterOffsets(lines: Array[String]): Array[Array[Int]] = {
    lines.map {
      _.scanLeft(0){ case (last, cur) => 
        cur match {
          case ' ' => last + spaceQuantaWidth
          case _ => last + gfont.letters(cur.toString).width 
        }
      }.toArray
    }
  }

  def setText(newText: String, noChange: Boolean = false): Unit = {
    val textChange = ExampleTextChangeAction(() => {
      text = newText
      lines = newText.split("\n", newText.length)
      lineLengths = lines.map(_.length)
      maxLineLen = lineLengths.max
      lineQuantaOffsets = getCharacterOffsets(lines)
      lineQuantaLengths = lineQuantaOffsets.map(line => line(line.length-1))
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
    g2.setStroke(lineStroke)
    for (i <- 0 until lines.length) {
      val row = lines(i)
      for (j <- 0 until row.length) {
        val char = row(j)
        val (x,y) = getCharPos(j, i)
        if (char != ' ') {
          val lead = gfont.letters(char.toString).leadingOffset
          val finalx = (x - lead*quantaWidth).toInt
          drawLetter(g2, char.toString, finalx, y)
        }
      }
    }
  }

  def drawCarrot(g2: Graphics2D): Unit = {
    val (x,y) = getCharPos(carrotCol, carrotRow)
    g2.setStroke(smallLineStroke)
    g2.setColor(Color.black)
    g2.draw(new Line2D.Double(x, y+(letterHeight*1.0/7.0).toInt, x,
      y+(letterHeight*5.0/7.0).toInt))
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
