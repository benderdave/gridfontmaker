package farg

import scala.collection.mutable.Map
import java.awt.{Dimension, Color, Font=>AWTFont, Graphics2D, Graphics,
 Rectangle}
import java.awt.event.{MouseMotionListener, MouseEvent, MouseListener,
 KeyListener, KeyEvent}
import java.awt.geom.{Ellipse2D, Line2D, Point2D, Rectangle2D}
import java.awt.RenderingHints._
import javax.swing.{JPanel, JPopupMenu, SwingUtilities}
import java.util.{Observer, Observable}

import GUIUtils.{MenuItem, interpolate}

// ----------------------------------------------------------------------------
class ObservableEditableLetter(updatables: Seq[Observer]) 
    extends Observable {
  updatables.foreach(addObserver(_))
  def changed(): Unit = {
    setChanged()
    notifyObservers()
  }
}

class EditableLetter(val ch: String, val up: EditPanel, updatables: 
    Seq[Observer]) extends JPanel with MouseMotionListener with MouseListener 
    with GlobalActionStack {
  import EditableLetter._
  import GridfontMaker._
  import GridfontMakerFrame._

  def letter = gfont.letters(ch)

  val observableLetter = new ObservableEditableLetter(updatables)

  val anchorMap: Map[Point2D, Int] = Map.empty
  var hitAnchorIndex: Option[Int] = None
  var dragLineStart: Option[(Int,Int)] = None
  var dragLineEnd: Option[(Int,Int)] = None

  val strokeMap: Map[Line2D, (Int,Int)] = Map.empty
  var hitStroke: Option[(Int,Int)] = None
  var hitPotentialStroke: Option[(Int,Int)] = None

  var showStrokeOrder: Boolean = false

  setBackground(Color.black)
  // NOTE: the following is just an initial guess that will be overridden by
  //  the layout manager
  setPreferredSize(new Dimension(400, (400/AspectRatio).toInt))
  addMouseMotionListener(this)
  addMouseListener(this)

  setFocusable(true)
  addKeyListener(new KeyListener {
    override def keyPressed(e: KeyEvent): Unit = {
      if (e.getKeyCode == KeyEvent.VK_SHIFT) up.shiftPressed = true
      else if (e.getKeyCode == KeyEvent.VK_O) {
        showStrokeOrder = true
        repaint()
      }
      else if (e.getKeyCode == KeyEvent.VK_UP) nudgeUp
      else if (e.getKeyCode == KeyEvent.VK_DOWN) nudgeDown
      else if (e.getKeyCode == KeyEvent.VK_LEFT) nudgeLeft
      else if (e.getKeyCode == KeyEvent.VK_RIGHT) nudgeRight
    }
    override def keyReleased(e: KeyEvent): Unit =
      if (e.getKeyCode == KeyEvent.VK_SHIFT) up.shiftPressed = false
      else if (e.getKeyCode == KeyEvent.VK_O) {
        showStrokeOrder = false
        repaint()
      }
    override def keyTyped(e: KeyEvent): Unit = {
      if (e.getKeyChar == 'c') clear()
      else if (e.getKeyChar == 'a') clearOthers
      else if (e.getKeyChar == 'v') flipY
      else if (e.getKeyChar == 'h') flipX
      else if (e.getKeyChar == 'r') rotate
      else if (e.getKeyChar == 'f')
        if (hitStroke.nonEmpty)
          flipStroke(hitStroke.get._1, hitStroke.get._2)
    }
  })

  val menuPopup = new JPopupMenu
  menuPopup.add(MenuItem("FlipX", () => flipX))
  menuPopup.add(MenuItem("FlipY", () => flipY))
  menuPopup.add(MenuItem("Rotate 180 degrees", () => rotate))
  menuPopup.add(MenuItem("Clear", () => clear()))
  menuPopup.add(MenuItem("Clear all others", () => clearOthers))
  menuPopup.add(MenuItem("Nudge Up", () => nudgeUp))
  menuPopup.add(MenuItem("Nudge Down", () => nudgeDown))
  menuPopup.add(MenuItem("Nudge Left", () => nudgeLeft))
  menuPopup.add(MenuItem("Nudge Right", () => nudgeRight))
  setComponentPopupMenu(menuPopup)

  def clear(noChange: Boolean = false): Unit = {
    val clearAction = ClearAction(this, () => {
      letter.clear
      observableLetter.changed()
      repaint()
    })
    if (noChange)
      clearAction.doit(true)
    else
      actions.add(clearAction)
  }

  def clearOthers: Unit = {
    actions.add(GridClearOthersAction(() => up.clearAll(Seq(this))))
  }

  def flipX: Unit = {
    actions.add(FlipAction(this, () => {
      letter.flipX
      observableLetter.changed()
      repaint()
    }))
  }

  def flipY: Unit = {
    actions.add(FlipAction(this, () => {
      letter.flipY
      observableLetter.changed()
      repaint()
    }))
  }

  def rotate: Unit = {
    actions.add(RotateAction(this, () => {
      letter.rotate
      observableLetter.changed()
      repaint()
    }))
  }

  def flipStroke(start: Int, end: Int): Unit = {
    actions.add(FlipStrokeAction(this, () => {
      if (hitStroke.nonEmpty) { // keep same stroke selected after flip
        val (hitst, hitend) = (hitStroke.get._1, hitStroke.get._2)
        if (hitst == start && hitend == end) hitStroke = Some((end, start))
      }
      letter.flipStroke(start, end)
      observableLetter.changed()
      repaint()
    }))
  }

  def nudgeUp: Unit = {
    actions.add(NudgeAction(this, () => {
      letter.nudgeUp
      observableLetter.changed()
      repaint()
    }))
  }

  def nudgeDown: Unit = {
    actions.add(NudgeAction(this, () => {
      letter.nudgeDown
      observableLetter.changed()
      repaint()
    }))
  }

  def nudgeLeft: Unit = {
    actions.add(NudgeAction(this, () => {
      letter.nudgeLeft
      observableLetter.changed()
      repaint()
    }))
  }

  def nudgeRight: Unit = {
    actions.add(NudgeAction(this, () => {
      letter.nudgeRight
      observableLetter.changed()
      repaint()
    }))
  }

  def setHitAnchorIndex(v: Option[Int]): Unit = {
    val oldHitAnchorIndex = hitAnchorIndex
    hitAnchorIndex = v
    if (oldHitAnchorIndex != v) repaint()
  }

  def setDragLineStart(v: Option[(Int,Int)]): Unit = {
    val oldDragLineStart = dragLineStart
    dragLineStart = v
    if (oldDragLineStart != v) repaint()
  }

  def setDragLineEnd(v: Option[(Int,Int)]): Unit = {
    val oldDragLineEnd = dragLineEnd
    dragLineEnd = v
    if (oldDragLineEnd != v) repaint()
  }

  def setHitStroke(v: Option[(Int,Int)]): Unit = {
    val oldHitStroke = hitStroke
    hitStroke = v
    if (oldHitStroke != v) repaint()
  }

  def setHitPotentialStroke(v: Option[(Int,Int)]): Unit = {
    val oldHitPotentialStroke = hitPotentialStroke
    hitPotentialStroke = v
    if (oldHitPotentialStroke != v) repaint()
  }

  def getOtherEndOfPotentialStroke(x: Int, y: Int, start: Int): Option[Int] = {
    val (startx, starty) = getAnchorPos(start)
    val bydist = (0 until NumAnchors).filter( anchor =>
      (start != anchor) && areNeighbors(start, anchor)
    ).map { anchorIdx =>
      val (anchorx, anchory) = getAnchorPos(anchorIdx)
      val seg = new Line2D.Double(startx, starty, anchorx, anchory)
      (seg.ptSegDist(x, y), anchorIdx)
    }.filter(_._1 < potLineSelectDist)
    if (bydist.nonEmpty) Some(bydist.min._2) else None
  }

  def detectMouseHit(x: Int, y: Int): Unit = {
    // look for anchor hit first, then existing stroke hit, then potential
    // stroke hit, and finally hit on bottom area
    val anchorDistances = anchorMap.map {
      case (p:Point2D, i:Int) => (p.distance(x, y), i)
    }
    val anchorHit = anchorDistances.filter(_._1 < anchorSelectDist)
    if (anchorHit.nonEmpty) {
      setHitAnchorIndex(Some(anchorHit.min._2))
      setHitStroke(None)
      setHitPotentialStroke(None)
      up.setSelected(this, false)
    } else {
      setHitAnchorIndex(None)
      val strokeHit = strokeMap.map {
        case (line: Line2D, i:(Int,Int)) => (line.ptSegDist(x, y), i)
      }.filter(_._1 < lineSelectDist)
      if (strokeHit.nonEmpty) {
        setHitStroke(Some(strokeHit.min._2))
        setHitPotentialStroke(None)
        up.setSelected(this, false)
      } else {
        setHitStroke(None)
        val closestAnchor = 
          if (anchorDistances.nonEmpty) anchorDistances.min._2
          else 0 // FIXME: why do I have to do this?
        val otherEnd = getOtherEndOfPotentialStroke(x, y, closestAnchor)
        if (otherEnd.nonEmpty) {
          setHitPotentialStroke(Some(closestAnchor, otherEnd.get))
          up.setSelected(this, false)
        } else {
          setHitPotentialStroke(None)
          val r = new Rectangle(0, getHeight-anchorBottomPad, getWidth,
            anchorBottomPad)
          up.setSelected(this, r.contains(x, y))
        }
      }
    }
  }

  override def mouseMoved(e: MouseEvent): Unit = {
    if (dragLineEnd.isEmpty) {
      val (x,y) = (e.getX, e.getY)
      detectMouseHit(x, y)
    }
  }

  override def mouseDragged(e: MouseEvent): Unit = {
    if (up.isDragging(this)) {
      up.drag(e)
    } else {
      setDragLineEnd( 
        if (dragLineStart.nonEmpty) Some((e.getX, e.getY))
        else None)
    }
  }

  override def mouseClicked(e: MouseEvent): Unit = {
    if (SwingUtilities.isLeftMouseButton(e)) {
      val (x,y) = (e.getX, e.getY)
      if (hitStroke.nonEmpty) {
        val (start, end) = hitStroke.get
        actions.add(StrokeAction(this, () => {
          letter.removeStroke(start, end)
          setHitStroke(None)
          observableLetter.changed()
          repaint()
        }))
      } else if (hitPotentialStroke.nonEmpty) {
        val (start, end) = hitPotentialStroke.get
        actions.add(StrokeAction(this, () => {
          letter.addStroke(start, end)
          setHitPotentialStroke(None)
          observableLetter.changed()
          repaint()
        }))
      }
      detectMouseHit(x, y)
    }
  }

  override def mouseEntered(e: MouseEvent): Unit = requestFocus
  override def mouseExited(e: MouseEvent): Unit = {
    if (dragLineStart.isEmpty) {
      setDragLineEnd(None)
      setHitAnchorIndex(None)
      setHitStroke(None)
      setHitPotentialStroke(None)
    }
    up.setSelected(this, false)
  }

  override def mousePressed(e: MouseEvent): Unit = {
    if (SwingUtilities.isLeftMouseButton(e)) {
      setDragLineStart(
        if (hitAnchorIndex.nonEmpty) Some(getAnchorPos(hitAnchorIndex.get))
        else None)
      if (hitAnchorIndex.nonEmpty)
        setHitPotentialStroke(None)
      if (up.isSelected(this)) {
        up.setDraggingLetter(this, true, 
          if (up.shiftPressed) DRAGMOVE else DRAGCOPY, e.getX, e.getY)
        up.setSelected(this, false)
      }
    }
  }

  override def mouseReleased(e: MouseEvent): Unit = {
    val (x,y) = (e.getX, e.getY)
    if (dragLineStart.nonEmpty && dragLineEnd.nonEmpty) {
      val end = anchorMap.map {
        case (a:Point2D, i:Int) => (a.distance(x, y), i)
      }.filter(_._1 < anchorSelectDist*2)
      if (end.nonEmpty) {
        val (newStart, newEnd) = (hitAnchorIndex.get, end.min._2)
        if (canConnect(newStart, newEnd)) {
          actions.add(StrokeAction(this, () => {
            for ((astart,aend) <- getStrokesBetween(newStart, newEnd))
              letter.addStroke(astart, aend)
            observableLetter.changed()
            repaint()
          }))
        }
      }
    }
    setDragLineStart(None)
    setDragLineEnd(None)
    setHitAnchorIndex(None)
    detectMouseHit(x, y)
    up.setDraggingLetter(this, false)
    up.shiftPressed = false
  }

  def showCharacterLabel(g2: Graphics2D): Unit = {
    // draw selection area
    g2.setColor(Color.darkGray)
    g2.fill(new Rectangle(2, getHeight-anchorBottomPad, getWidth-4,
      anchorBottomPad-2))
    // draw letter label
    g2.setColor(Color.white)
    g2.setFont(letterLabelFont)
    val h = getHeight()
    val metrics = g2.getFontMetrics(g2.getFont)
    val r = metrics.getStringBounds(letter.ch, g2).getBounds()
    val xpos = (getWidth()/2 - r.getWidth()/2).toInt
    val ypos = h - letterLabelPad
    g2.drawString(letter.ch, xpos, ypos)
  }

  def colStep = (getWidth()-2*anchorColPad) / NumCols
  def rowStep = (getHeight()-2*anchorRowPad-anchorBottomPad) / NumRows

  def getAnchorPos(index: Int): (Int,Int) = {
    val rStep = rowStep
    val rowPad = rStep/2
    val cStep = colStep
    val colPad = cStep/2
    val (row, col) = getRowCol(index)
    (anchorColPad+colPad+col*cStep, anchorRowPad+rowPad+row*rStep)
  }

  def getStrokesBetween(startIdx: Int, endIdx: Int): Seq[(Int,Int)] = {
    val (startRow, startCol) = getRowCol(startIdx)
    val (endRow, endCol) = getRowCol(endIdx)
    val (rowDir, colDir) =
      (if (startRow > endRow) -1 else 1, if (startCol > endCol) -1 else 1)
    val n = if (startRow != endRow) Math.abs(startRow - endRow)+1
            else Math.abs(startCol - endCol)+1
    val rowIdxs = if (startRow == endRow) (1 to n).map(i => startRow).toList
                  else (startRow to endRow by rowDir).toList
    val colIdxs = if (startCol == endCol) (1 to n).map(i => startCol).toList
                  else (startCol to endCol by colDir).toList
    val points = rowIdxs zip colIdxs
    for (i <- 0 until points.length-1) yield {
      val (x1,y1) = points(i)
      val (x2,y2) = points(i+1)
      (rowColToIndex(x1,y1), rowColToIndex(x2,y2))
    }
  }

  def areNeighbors(startIdx: Int, endIdx: Int): Boolean = {
    val (startRow, startCol) = getRowCol(startIdx)
    val (endRow, endCol) = getRowCol(endIdx)
    val (rowAbsDelta, colAbsDelta) = 
      (Math.abs(endRow - startRow), Math.abs(endCol - startCol))
    rowAbsDelta <= 1 && colAbsDelta <= 1
  }

  def canConnect(startIdx: Int, endIdx: Int): Boolean = {
    val (startRow, startCol) = getRowCol(startIdx)
    val (endRow, endCol) = getRowCol(endIdx)
    val (rowAbsDelta, colAbsDelta) = 
      (Math.abs(endRow - startRow), Math.abs(endCol - startCol))
    val neighbor = rowAbsDelta <= 1 && colAbsDelta <= 1
    val diagonal = rowAbsDelta == colAbsDelta
    val straight = rowAbsDelta == 0 || colAbsDelta == 0
    (startIdx != endIdx) && (neighbor || diagonal || straight)
  }

  def paintCentralZone(g2: Graphics2D): Unit = {
    val centerRow = NumRows/2
    val centralTopLeft = rowColToIndex(centerRow-1, 0)
    val centralLowerRight = rowColToIndex(centerRow+1, NumCols-1)
    val (x, y) = getAnchorPos(centralTopLeft)
    g2.setColor(centralZoneColor)
    g2.fill(new Rectangle2D.Double(x, y, colStep*2, rowStep*2))
  }

  def paintAnchors(g2: Graphics2D): Unit = {
    anchorMap.clear
    g2.setColor(Color.gray)
    for (index <- 0 until NumAnchors) {
      val (x, y) = getAnchorPos(index)
      anchorMap(new Point2D.Double(x,y)) = index
      if (up.showAnchors) {
        if (hitAnchorIndex.nonEmpty &&
            ((hitAnchorIndex.get == index) ||
             (dragLineStart.nonEmpty && 
              canConnect(index, hitAnchorIndex.get)))) {
          g2.setColor(addColor)
          g2.fill(new Ellipse2D.Double(x-hlAnchorSize/2, y-hlAnchorSize/2,
            hlAnchorSize, hlAnchorSize))
          g2.setColor(Color.gray)
        } else {
          g2.fill(new Ellipse2D.Double(x-anchorSize/2, y-anchorSize/2,
            anchorSize, anchorSize))
        }
      }
    }
  }

  def paintStrokes(g2: Graphics2D): Unit = {
    strokeMap.clear

    if (hitPotentialStroke.nonEmpty) {
      g2.setStroke(largeLineStroke)
      val (start, end) = hitPotentialStroke.get
      val (startx, starty) = getAnchorPos(start)
      val (endx, endy) = getAnchorPos(end)
      g2.setColor(addColor)
      g2.draw(new Line2D.Double(startx, starty, endx, endy))
    }

    g2.setStroke(lineStroke)

    var i = 1
    for ((start, end) <- letter.getStrokes) {
      val (startx, starty) = getAnchorPos(start)
      val (endx, endy) = getAnchorPos(end)
      if (hitStroke.nonEmpty &&
          start == hitStroke.get._1 && end == hitStroke.get._2)
          g2.setColor(removeColor)
      else
        g2.setColor(Color.white)
      val strokeLine = new Line2D.Double(startx, starty, endx, endy)
      g2.draw(strokeLine)
      strokeMap(strokeLine) = (start, end)
      if (showStrokeOrder || (hitStroke.nonEmpty && 
          start == hitStroke.get._1 && end == hitStroke.get._2)) {
        val (tx, ty) = interpolate(0.2, startx, starty, endx, endy)
        g2.setColor(Color.white)
        g2.setFont(letterLabelFont)
        g2.drawString(s"$i", tx.toInt, ty.toInt)
      }
      i += 1
    }
  }

  def paintDragLine(g2: Graphics2D): Unit = {
    if (dragLineStart.nonEmpty && dragLineEnd.nonEmpty) {
      g2.setColor(addColor)
      g2.setStroke(lineStroke)
      val (startx, starty) = dragLineStart.get
      val (endx, endy) = dragLineEnd.get
      g2.draw(new Line2D.Double(startx, starty, endx, endy))
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val g2 = g.asInstanceOf[Graphics2D]
    g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
    g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    if (up.showCentralZone)
      paintCentralZone(g2)
    paintAnchors(g2)
    paintStrokes(g2)
    paintDragLine(g2)
    showCharacterLabel(g2)
  }
}

object EditableLetter {
  import GridfontMaker._

  var addColor = new Color(50, 200, 50)
  var removeColor = new Color(200, 50, 50)
  val centralZoneColor = new Color(37, 35, 38)
  var letterLabelFont = new AWTFont("System", AWTFont.ITALIC, 16)
  var letterLabelPad = 10
  val anchorRowPad = 2
  val anchorColPad = 2

  var anchorSize = 0
  var hlAnchorSize = 0
  var lineSelectDist = 0.0
  var potLineSelectDist = 0.0
  var anchorSelectDist = 0.0
  var anchorBottomPad = 0

  val NODRAG: Int = 0
  val DRAGCOPY: Int = 1
  val DRAGMOVE: Int = 2

  def getPreferredSizeGivenBounds(w: Int, h: Int, pad: Int): Dimension = {
    val newAspect = w.toDouble/h.toDouble
    val bestNumLettersPerRow = (1 until 26).map { n =>
      val nAspect = (AspectRatio*n)/((26+n-1)/n)
      (newAspect-nAspect, n)
    }.filter(_._1 >= 0).min._2
    val n = (bestNumLettersPerRow+1).toDouble

    val (neww, newh) = ((w/n).toInt-pad, (h/(26/n)).toInt-pad)
    val newhLessPad = newh-anchorBottomPad
    if (neww/AspectRatio < newhLessPad) {
      // too tall for width? adjust height to match width
      new Dimension(neww, (neww/AspectRatio).toInt + anchorBottomPad)
    } else {
      // too wide for height? adjust width to match height
      new Dimension((newhLessPad*AspectRatio).toInt, newh)
    }
  }

  def updateLetterConstants(d: Dimension): Unit = {
    anchorSize = Math.max((d.getWidth / 17).toInt, 6)
    hlAnchorSize = (anchorSize*1.5).toInt
    lineSelectDist = anchorSize*1.2
    potLineSelectDist = anchorSize
    anchorSelectDist = anchorSize*1.7
    anchorBottomPad = (d.getHeight / 17).toInt
    letterLabelFont = letterLabelFont.deriveFont(
      Math.max(d.getWidth/10.0, 4.0).toFloat
    )
    letterLabelPad = (d.getWidth / 25).toInt
  }
}
