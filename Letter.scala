package farg

import scala.collection.mutable.ArrayBuffer

// ----------------------------------------------------------------------------
case class Stroke(start: Int, end: Int)

// ----------------------------------------------------------------------------
case class Letter(ch: String, strokes: ArrayBuffer[Stroke] = ArrayBuffer.empty) {
  import GridfontMaker.{NumRows, NumCols, getRowCol, rowColToIndex}
  import Letter.getStrokesRowsCols

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

  def isColInAnyStroke(col: Int): Boolean = 
    getStrokesRowsCols(strokes).exists { 
      case (_, startRow, startCol, endRow, endCol) =>
        col == startCol || col == endCol
    }

  def leadingOffset: Int = (0 until NumCols).indexWhere(isColInAnyStroke(_))
  def trailingOffset: Int =
    (NumCols-1 to 0 by -1).indexWhere(isColInAnyStroke(_))
  def width: Int = NumCols - (leadingOffset + trailingOffset)

  def clear: Unit = strokes.clear

  def flipX: Unit =
    getStrokesRowsCols(strokes).map { 
      case (i, startRow, startCol, endRow, endCol) =>
        strokes(i) = Stroke(rowColToIndex(startRow, (NumCols-1)-startCol), 
          rowColToIndex(endRow, (NumCols-1)-endCol))
    }

  def flipY: Unit =
    getStrokesRowsCols(strokes).map { 
      case (i, startRow, startCol, endRow, endCol) =>
        strokes(i) = Stroke(rowColToIndex((NumRows-1)-startRow, startCol), 
          rowColToIndex((NumRows-1)-endRow, endCol))
    }

  def rotate: Unit = {
    flipX
    flipY
  }

  def flipStroke(start: Int, end: Int): Unit = {
    val i = strokes.indexWhere(s => start == s.start && end == s.end)
    if (i >= 0) strokes(i) = Stroke(end, start)
  }

  def nudgeUp: Unit = {
    val strokesCopy = strokes.toList
    strokes.clear
    getStrokesRowsCols(strokesCopy).map { 
      case (_, startRow, startCol, endRow, endCol) =>
        if (startRow > 0 && endRow > 0)
          strokes += Stroke(rowColToIndex(startRow-1, startCol), 
           rowColToIndex(endRow-1, endCol))
    }
  }

  def nudgeDown: Unit = {
    val strokesCopy = strokes.toList
    strokes.clear
    getStrokesRowsCols(strokesCopy).map { 
      case (_, startRow, startCol, endRow, endCol) =>
        if (startRow < NumRows-1 && endRow < NumRows-1)
          strokes += Stroke(rowColToIndex(startRow+1, startCol), 
           rowColToIndex(endRow+1, endCol))
    }
  }

  def nudgeLeft: Unit = {
    val strokesCopy = strokes.toList
    strokes.clear
    getStrokesRowsCols(strokesCopy).map { 
      case (_, startRow, startCol, endRow, endCol) =>
        if (startCol > 0 && endCol > 0)
          strokes += Stroke(rowColToIndex(startRow, startCol-1), 
           rowColToIndex(endRow, endCol-1))
    }
  }

  def nudgeRight: Unit = {
    val strokesCopy = strokes.toList
    strokes.clear
    getStrokesRowsCols(strokesCopy).map { 
      case (_, startRow, startCol, endRow, endCol) =>
        if (startCol < NumCols-1 && endCol < NumCols-1)
          strokes += Stroke(rowColToIndex(startRow, startCol+1), 
           rowColToIndex(endRow, endCol+1))
    }
  }
}

object Letter {
  import GridfontMaker.getRowCol

  def getStrokesRowsCols(strokes: Seq[Stroke]) = {
    for ((stroke, i) <- strokes.zipWithIndex) yield {
      val (startRow, startCol) = getRowCol(stroke.start)
      val (endRow, endCol) = getRowCol(stroke.end)
      (i, startRow, startCol, endRow, endCol)
    }
  }
}
