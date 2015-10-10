package farg

import scala.collection.mutable.ArrayBuffer

// ----------------------------------------------------------------------------
case class Stroke(start: Int, end: Int)

// ----------------------------------------------------------------------------
case class Letter(ch: String, strokes: ArrayBuffer[Stroke] = ArrayBuffer.empty) {
  import GridfontMaker._

  def addStroke(start: Int, end: Int): Unit = {
    if (start != end) {
      removeStroke(start, end)
      strokes += Stroke(start, end)
    }
  }

  def removeStroke(start: Int, end: Int): Unit = {
    strokes -= Stroke(start, end)
    strokes -= Stroke(end, start)
  }

  def getStrokes = for (stroke <- strokes) yield (stroke.start, stroke.end)

  def clear: Unit = strokes.clear

  def flipX: Unit = {
    for (i <- 0 until strokes.length) {
      val s = strokes(i)
      val (startrow, startcol) = getRowCol(s.start)
      val (endrow, endcol) = getRowCol(s.end)
      strokes(i) = Stroke(rowColToIndex(startrow, (NumCols-1)-startcol), 
        rowColToIndex(endrow, (NumCols-1)-endcol))
    }
  }

  def flipY: Unit = {
    for (i <- 0 until strokes.length) {
      val s = strokes(i)
      val (startrow, startcol) = getRowCol(s.start)
      val (endrow, endcol) = getRowCol(s.end)
      strokes(i) = Stroke(rowColToIndex((NumRows-1)-startrow, startcol), 
        rowColToIndex((NumRows-1)-endrow, endcol))
    }
  }

  def flipStroke(start: Int, end: Int): Unit = {
    val i = strokes.indexWhere(s => start == s.start && end == s.end)
    if (i >= 0) strokes(i) = Stroke(end, start)
  }

  def nudgeUp: Unit = {
    val strokesCopy = strokes.toList
    strokes.clear
    for (s <- strokesCopy) {
      val (startrow, startcol) = getRowCol(s.start)
      val (endrow, endcol) = getRowCol(s.end)
      if (startrow > 0 && endrow > 0)
        strokes += Stroke(rowColToIndex(startrow-1, startcol), 
         rowColToIndex(endrow-1, endcol))
    }
  }

  def nudgeDown: Unit = {
    val strokesCopy = strokes.toList
    strokes.clear
    for (s <- strokesCopy) {
      val (startrow, startcol) = getRowCol(s.start)
      val (endrow, endcol) = getRowCol(s.end)
      if (startrow < NumRows-1 && endrow < NumRows-1)
        strokes += Stroke(rowColToIndex(startrow+1, startcol), 
         rowColToIndex(endrow+1, endcol))
    }
  }

  def nudgeLeft: Unit = {
    val strokesCopy = strokes.toList
    strokes.clear
    for (s <- strokesCopy) {
      val (startrow, startcol) = getRowCol(s.start)
      val (endrow, endcol) = getRowCol(s.end)
      if (startcol > 0 && endcol > 0)
        strokes += Stroke(rowColToIndex(startrow, startcol-1), 
         rowColToIndex(endrow, endcol-1))
    }
  }

  def nudgeRight: Unit = {
    val strokesCopy = strokes.toList
    strokes.clear
    for (s <- strokesCopy) {
      val (startrow, startcol) = getRowCol(s.start)
      val (endrow, endcol) = getRowCol(s.end)
      if (startcol < NumCols-1 && endcol < NumCols-1)
        strokes += Stroke(rowColToIndex(startrow, startcol+1), 
         rowColToIndex(endrow, endcol+1))
    }
  }
}
