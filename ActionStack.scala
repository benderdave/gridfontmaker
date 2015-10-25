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
  def text: String
  def doit(firstTime: Boolean): Action
  def undo: Action
  def merge(newer: Action): Action = newer
  def canMergeWith(other: Action): Boolean = false
}

abstract trait MergableAction extends Action {
  override def merge(newer: Action): Action
  override def canMergeWith(other: Action): Boolean = getClass == other.getClass
}

// ----------------------------------------------------------------------------
class ActionStack(undoGui: JMenuItem, redoGui: JMenuItem) {
  var future: ArrayStack[Action] = new ArrayStack[Action]
  var past: ArrayStack[Action] = new ArrayStack[Action]

  updateMenuItems
  
  def reset: Unit = {
    future.clear
    past.clear
    updateMenuItems
  }
  
  def canMerge(current: Action): Boolean = {
    if (past.isEmpty) false
    else past.head.canMergeWith(current)
  }

  def add(a: Action): Action = {
    future.clear
    val toinsert = if (canMerge(a)) past.pop.merge(a)
                   else a
    past.push(toinsert.doit(firstTime=true))
    updateMenuItems
    toinsert
  }
  
  def undo: Unit = 
    if (past.nonEmpty) {
      future.push(past.pop.undo)
      updateMenuItems
    }
  
  def redo: Unit =
    if (future.nonEmpty) {
      past.push(future.pop.doit(false))
      updateMenuItems
    }

  def updateMenuItem(kind: String, gui: JMenuItem, time: ArrayStack[Action]):
      Unit = {
    gui.setText(if (time.nonEmpty) s"$kind ${time.head.text}" else kind)
    gui.setEnabled(if (time.nonEmpty) true else false)
  }

  def updateMenuItems: Unit = {
    updateMenuItem("Undo", undoGui, past)
    updateMenuItem("Redo", redoGui, future)
  }
}

// ----------------------------------------------------------------------------
// specific actions from here on

class LetterChangeAction(msg: String, guiLetter: EditableLetter, f: () => Unit) 
    extends Action with GlobalFont {
  val ch = guiLetter.letter.ch
  val before = toJson(guiLetter.letter)

  def text: String = msg

  override def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  override def undo: Action = {
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

case class RotateAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("rotate letter", guiLetter, f)

case class FlipStrokeAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("flip stroke", guiLetter, f)

case class NudgeAction(guiLetter: EditableLetter, f: () => Unit) 
  extends LetterChangeAction("nudge letter", guiLetter, f)

class OrderAction(msg: String, panel: EditPanel, f: () => Unit) extends Action {
  val orderBefore = panel.getComponents()
    .map(_.asInstanceOf[EditableLetter].letter.ch)

  def text: String = msg

  override def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  override def undo: Action = {
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

  override def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  override def undo: Action = {
    GridfontMakerFrame.gfont = fromJson[Font](before)
    GridfontMakerFrame.gui.repaint()
    this
  }
}

case class GridClearAction(f: () => Unit) extends GridAction("clear grid", f)
case class GridClearOthersAction(f: () => Unit) 
  extends GridAction("clear other letters", f)

case class NameChangeAction(f: (Boolean) => Unit) extends MergableAction 
    with GlobalFont {
  var before = gfont.name
  def text: String = "change name"

  override def doit(firstTime: Boolean): Action = {
    f(firstTime)
    this
  }

  override def undo: Action = {
    GridfontMakerFrame.gui.alphAndNameArea.namePanel.setFontName(before, true)
    this
  }

  override def merge(newer: Action): Action = {
    newer.asInstanceOf[NameChangeAction].before = before
    newer
  }
}

case class ExampleTextChangeAction(f: () => Unit) extends MergableAction 
    with GlobalFont {
  var before = gfont.example_text
  def text: String = "change example text"

  override def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  override def undo: Action = {
    GridfontMakerFrame.gui.textPanel.textArea.setText(before, true)
    this
  }

  override def merge(newer: Action): Action = {
    newer.asInstanceOf[ExampleTextChangeAction].before = before
    newer
  }
}

case class SizeChangeAction(textArea: GridfontTextArea, f: () => Unit)
    extends MergableAction {
  var before = textArea.gfontSize
  def text: String = "change font size"

  override def doit(firstTime: Boolean): Action = {
    f()
    this
  }

  override def undo: Action = {
    textArea.setSize(before)
    textArea.calculatePreferredSize
    textArea.repaint()
    textArea.getParent().revalidate
    this
  }

  override def merge(newer: Action): Action = {
    newer.asInstanceOf[SizeChangeAction].before = before
    newer
  }
}
