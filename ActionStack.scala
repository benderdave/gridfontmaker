package farg

import com.owlike.genson.defaultGenson._
import javax.swing.JMenuItem
import scala.collection.mutable.ArrayStack

// ----------------------------------------------------------------------------
trait GlobalActionStack {
  def actions: ActionStack = GridfontMakerFrame.gui.actionStack
}

// ----------------------------------------------------------------------------
abstract trait Action {
  val mergable = false
  def text: String
  def doit(firstTime: Boolean): Action
  def undo: Action
  def merge(newer: Action): Action = ???
}

// ----------------------------------------------------------------------------
class ActionStack(undoGui: JMenuItem, redoGui: JMenuItem) {
  var future: ArrayStack[Action] = new ArrayStack[Action]
  var past: ArrayStack[Action] = new ArrayStack[Action]

  updateMenu
  
  def reset: Unit = {
    future.clear
    past.clear
    updateMenu
  }
  
  def canMerge(newer: Action): Boolean = {
    if (past.isEmpty) false
    else {
      val older = past.head
      older.getClass == newer.getClass &&
      older.mergable && newer.mergable
    }
  }

  def add(a: Action): Action = {
    future.clear
    val toinsert = if (canMerge(a)) past.pop.merge(a)
                   else a
    past.push(toinsert.doit(true))
    updateMenu
    toinsert
  }
  
  def undo: Unit = 
    if (past.nonEmpty) {
      future.push(past.pop.undo)
      updateMenu
    }
  
  def redo: Unit =
    if (future.nonEmpty) {
      past.push(future.pop.doit(false))
      updateMenu
    }

  def updateMenu: Unit = {
    if (past.nonEmpty) {
      undoGui.setText(s"Undo ${past.head.text}")
      undoGui.setEnabled(true)
    } else {
      undoGui.setText("Undo")
      undoGui.setEnabled(false)
    }

    if (future.nonEmpty) {
      redoGui.setText(s"Redo ${future.head.text}")
      redoGui.setEnabled(true)
    } else {
      redoGui.setText("Redo")
      redoGui.setEnabled(false)
    }
  }
}

// ----------------------------------------------------------------------------
class LetterChangeAction(msg: String, guiLetter: EditableLetter, f: () => Unit) 
    extends Action with GlobalFont {
  val ch = guiLetter.letter.ch
  val before = toJson(guiLetter.letter)

  def text: String = msg

  def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  def undo: Action = {
    gfont.letters(ch) = fromJson[Letter](before)
    guiLetter.repaint()
    guiLetter.observableLetter.changed()
    this
  }
}

case class StrokeAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("stroke", guiLetter, f)

case class CopyAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("copy", guiLetter, f)

case class ClearAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("clear letter", guiLetter, f)

case class FlipAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("flip letter", guiLetter, f)

case class FlipStrokeAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("flip stroke", guiLetter, f)

case class NudgeAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("nudge letter", guiLetter, f)

class OrderAction(msg: String, panel: EditPanel, f: () => Unit) extends Action {
  val orderBefore = panel.getComponents()
    .map(_.asInstanceOf[EditableLetter].letter.ch)

  def text: String = msg

  def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  def undo: Action = {
    panel.removeAll()
    orderBefore.foreach(ch => panel.add(panel.guiLetters(ch)))
    panel.revalidate()
    panel.repaint()
    this
  }
}

case class GridSortAction(panel: EditPanel, f: () => Unit) 
  extends OrderAction("sort grid", panel, f)

case class MoveAction(panel: EditPanel, f: () => Unit) 
  extends OrderAction("move letter", panel, f)

class GridAction(msg: String, f: () => Unit) extends Action with GlobalFont {
  val before = toJson(gfont)

  def text: String = msg

  def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  def undo: Action = {
    GridfontMakerFrame.gfont = fromJson[Font](before)
    GridfontMakerFrame.gui.repaint()
    this
  }
}

case class GridClearAction(f: () => Unit) extends GridAction("clear grid", f)

case class NameChangeAction(f: (Boolean) => Unit) extends Action 
    with GlobalFont {
  override val mergable = true
  var before = gfont.name
  def text: String = "change name"

  def doit(firstTime: Boolean): Action = {
    f(firstTime)
    this
  }

  def undo: Action = {
    GridfontMakerFrame.gui.alphAndNameArea.namePanel.setFontName(before, true)
    this
  }

  override def merge(newer: Action): Action = {
    newer.asInstanceOf[NameChangeAction].before = before
    newer
  }
}

case class ExampleTextChangeAction(f: () => Unit) extends Action 
    with GlobalFont {
  override val mergable = true
  var before = gfont.example_text
  def text: String = "change example text"

  def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  def undo: Action = {
    GridfontMakerFrame.gui.textPanel.setText(before, true)
    this
  }

  override def merge(newer: Action): Action = {
    newer.asInstanceOf[ExampleTextChangeAction].before = before
    newer
  }
}

case class SizeChangeAction(textPanel: GridfontTextArea, f: () => Unit)
    extends Action {
  override val mergable = true
  var before = textPanel.gfontSize
  def text: String = "change font size"

  def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  def undo: Action = {
    textPanel.setSize(before)
    textPanel.calculatePreferredSize
    textPanel.repaint()
    textPanel.getParent().revalidate
    this
  }

  override def merge(newer: Action): Action = {
    newer.asInstanceOf[SizeChangeAction].before = before
    newer
  }
}
