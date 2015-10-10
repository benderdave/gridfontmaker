package farg

import java.awt.Toolkit
import javax.swing.{JMenuBar, JMenu, KeyStroke}
import java.awt.event.WindowEvent

import GUIUtils.MenuItem

// ----------------------------------------------------------------------------
class GridfontMenuBar(gui: GridfontMakerFrame) extends JMenuBar with
    GlobalActionStack {
  val shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()

  val fileMenu = new JMenu("File")
  add(fileMenu)

  val newItem = MenuItem("New", () => gui.newGrid)
  newItem.setAccelerator(KeyStroke.getKeyStroke('N', shortcutKey))
  fileMenu.add(newItem)

  val openItem = MenuItem("Open..", () => gui.openFile)
  openItem.setAccelerator(KeyStroke.getKeyStroke('O', shortcutKey))
  fileMenu.add(openItem)

  val saveItem = MenuItem("Save", () => gui.saveFile)
  saveItem.setAccelerator(KeyStroke.getKeyStroke('S', shortcutKey))
  fileMenu.add(saveItem)

  val saveAsItem = MenuItem("Save as..", () => gui.saveFileAs)
  saveAsItem.setAccelerator(KeyStroke.getKeyStroke('A', shortcutKey))
  fileMenu.add(saveAsItem)

  fileMenu.addSeparator
  val quitItem = MenuItem("Quit", () => 
    gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING)))
  quitItem.setAccelerator(KeyStroke.getKeyStroke('Q', shortcutKey))
  fileMenu.add(quitItem)

  val editMenu = new JMenu("Edit")
  add(editMenu)

  val undo = MenuItem("Undo", () => gui.actionStack.undo)
  undo.setAccelerator(KeyStroke.getKeyStroke('Z', shortcutKey))
  editMenu.add(undo)

  val redo = MenuItem("Redo", () => gui.actionStack.redo)
  redo.setAccelerator(KeyStroke.getKeyStroke('R', shortcutKey))
  editMenu.add(redo)

  val gridMenu = new JMenu("Grid")
  add(gridMenu)

  val clearItem = MenuItem("Clear", () => 
    actions.add(GridClearAction(() => gui.editPanel.clearAll)))
  clearItem.setAccelerator(KeyStroke.getKeyStroke('C', shortcutKey))
  gridMenu.add(clearItem)

  val sortItem = MenuItem("Sort", () =>
    actions.add(GridSortAction(gui.editPanel, () => gui.editPanel.sortAll)))
  gridMenu.add(sortItem)

  var helpFrame: Option[HelpFrame] = None
  val helpItem = MenuItem("Help", () => {
    if (helpFrame.isEmpty) {
      val (w, h) = ((getParent.getWidth/3).toInt, (getParent.getHeight*0.75).toInt)
      helpFrame = Some(new HelpFrame(this, w, h))
    }
  })
  add(helpItem)
}
