package farg

import scala.collection.mutable.Map
import java.awt.{Color, ComponentOrientation, Graphics2D, Graphics,
 BasicStroke, FlowLayout, Point}
import java.awt.BasicStroke._
import java.awt.event.MouseEvent
import java.awt.geom.{Line2D, Rectangle2D}
import javax.swing.JPanel 
import java.util.{Observer, Observable}

// ----------------------------------------------------------------------------
class EditPanel(var updatables: Seq[Observer]) extends JPanel with GlobalFont with
    GlobalActionStack {
  import EditableLetter._

  setBackground(Color.gray)
  setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT)
  val pad = 5
  setLayout(new FlowLayout(FlowLayout.CENTER, pad, pad))
  val guiLetters: Map[String, EditableLetter] = Map.empty

  for (ch <- gfont.letters.keys.toList.sorted) {
    val letter = new EditableLetter(ch, this, updatables)
    add(letter)
    guiLetters(ch) = letter
  }

  var shiftPressed: Boolean = false
  var showAnchors: Boolean = true
  var showCentralZone: Boolean = true

  var selectedLetter: Option[EditableLetter] = None
  var draggingLetter: Option[EditableLetter] = None
  var dragStart: (Int, Int) = (0,0)
  var dragDelta: (Int, Int) = (0,0)
  var dragType: Int = NODRAG
  var dragTargets: Seq[EditableLetter] = Seq.empty

  def clearAll(except: Seq[EditableLetter] = Seq.empty): Unit = {
    guiLetters.filter { case (ch: String, letter: EditableLetter) =>
      !except.contains(letter)
    }.foreach(_._2.clear(true))
    repaint()
  }

  def sortAll: Unit = {
    removeAll()
    guiLetters.toList.sortBy(_._1).foreach{ case (ch,letter) => add(letter) }
    revalidate()
    repaint()
  }

  def setSelected(letter: EditableLetter, selected: Boolean): Unit = {
    val currentSelectedLetter = selectedLetter
    if (selected)
      selectedLetter = Some(letter)
    else if (selectedLetter == Some(letter))
      selectedLetter = None
    if (currentSelectedLetter != selectedLetter) repaint()
  }

  def isSelected(letter: EditableLetter): Boolean =
    selectedLetter == Some(letter)

  def setDraggingLetter(letter: EditableLetter, dragging: Boolean, 
      dragt: Int = NODRAG, x: Int = 0, y: Int = 0): Unit = {
    val currentDraggingLetter = draggingLetter
    if (dragging) {
      draggingLetter = Some(letter)
      dragStart = (x, y)
      dragDelta = (0, 0)
      updateDragTargets(letter.getLocationOnScreen)
      dragType = dragt
    } else if (draggingLetter == Some(letter)) {
      if (dragTargets.nonEmpty) {
        dragType match {
          case DRAGCOPY =>
            copyLetterTo(draggingLetter.get, dragTargets.head)
          case DRAGMOVE =>
            if (dragTargets.length == 2) {
              val todrag = draggingLetter.get
              if (todrag != dragTargets(0) && todrag != dragTargets(1))
                moveLetterBetween(draggingLetter.get, dragTargets(0), 
                  dragTargets(1))
            }
          case NODRAG =>
        }
      }
      draggingLetter = None
      dragStart = (0, 0)
      dragDelta = (0, 0)
      dragType = NODRAG
    }
    if (currentDraggingLetter != draggingLetter) repaint()
  }

  def isDragging(letter: EditableLetter): Boolean =
    draggingLetter == Some(letter)

  def updateDragTargets(mloc: Point): Unit = {
    val maxDist =
      if (draggingLetter.nonEmpty) draggingLetter.get.getHeight/2
      else 0
    dragTargets = getComponents.map { case letter: EditableLetter =>
      val loc = letter.getLocationOnScreen()
      val (cx, cy) = (loc.x + letter.getWidth/2, loc.y + letter.getHeight/2)
      val (mx, my) = (mloc.x, mloc.y-letter.getHeight/2)
      val dist = Math.sqrt((mx-cx)*(mx-cx) + (my-cy)*(my-cy))
      (dist, letter)
    }.filter(_._1 < maxDist).sortBy(_._1).slice(0,2).unzip._2
  }

  def drag(e: MouseEvent): Unit = {
    val (x, y) = (e.getX, e.getY)
    dragDelta = (x - dragStart._1, y - dragStart._2)
    updateDragTargets(e.getLocationOnScreen)
    repaint()
  }

  def copyLetterTo(src: EditableLetter, dst: EditableLetter): Unit = {
    if (src.letter.ch != dst.letter.ch) {
      actions.add(CopyAction(dst, () => {
        dst.letter.clear
        dst.letter.strokes.appendAll { 
          for ((begin, end) <- src.letter.getStrokes)
            yield Stroke(begin, end) 
        }
        dst.observableLetter.changed
        dst.requestFocusInWindow
      }))
    }
  }

  def moveLetterBetween(src: EditableLetter, dst1: EditableLetter, 
      dst2: EditableLetter): Unit = {
    actions.add(MoveAction(this, () => {
      val allLetters = getComponents()
      val srcIdx = allLetters.indexOf(src)
      val dst1Idx = allLetters.indexOf(dst1)
      val dst2Idx = allLetters.indexOf(dst2)
      val lowdst = Math.min(dst1Idx, dst2Idx)
      removeAll()
      for (i <- 0 until allLetters.length) {
        if (i != srcIdx) {
          add(allLetters(i))
          if (i == lowdst) add(src)
        }
      }
      revalidate()
      repaint()
      src.requestFocusInWindow
    }))
  }

  def updateLetterSize: Unit = {
    val d = getPreferredSizeGivenBounds(getWidth, getHeight, pad)
    guiLetters.foreach(_._2.setPreferredSize(d))
    updateLetterConstants(d)
  }

  override def setBounds(x: Int, y: Int, w: Int, h: Int): Unit = {
    super.setBounds(x,y,w,h)
    updateLetterSize
  }

  override def paint(g: Graphics): Unit = {
    super.paint(g)
    val g2 = g.asInstanceOf[Graphics2D]

    if (selectedLetter.nonEmpty || draggingLetter.nonEmpty) {
      g2.setStroke(new BasicStroke(3.0f, CAP_ROUND, JOIN_BEVEL))
      g2.setColor(Color.white)
      val r = 
        if (selectedLetter.nonEmpty) selectedLetter.get.getBounds()
        else draggingLetter.get.getBounds()
      r.grow(3, 3)
      r.translate(dragDelta._1, dragDelta._2)
      g2.draw(new Rectangle2D.Double(r.getX, r.getY, r.getWidth, r.getHeight))
      dragType match {
        case DRAGCOPY =>
          if (dragTargets.nonEmpty) {
            g2.setColor(Color.green)
            val r = dragTargets.head.getBounds()
            r.grow(3, 3)
            g2.draw(new Rectangle2D.Double(r.getX, r.getY, r.getWidth, r.getHeight))
          }
        case DRAGMOVE =>
          if (dragTargets.length == 2) {
            g2.setColor(Color.green)
            val r = dragTargets(0).getBounds()
            r.add(dragTargets(1).getBounds())
            val midx = r.getX + r.getWidth/2
            g2.draw(new Line2D.Double(midx, r.getY-2, midx, r.getY+r.getHeight+2))
          }
        case NODRAG =>
      }
    }
  }
}
