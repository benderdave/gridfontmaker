package farg

import java.awt.Toolkit
import javax.swing.{JMenuBar, JMenu, KeyStroke}
import java.awt.event.WindowEvent

import GUIUtils.{MenuItem, MenuItemRef}

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
  val compareItem = MenuItem("Compare with..", () => gui.addFontCompare)
  compareItem.setAccelerator(KeyStroke.getKeyStroke('M', shortcutKey))
  fileMenu.add(compareItem)

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
    actions.add(GridClearAction(() => gui.editPanel.clearAll())))
  clearItem.setAccelerator(KeyStroke.getKeyStroke('C', shortcutKey))
  gridMenu.add(clearItem)

  val sortItem = MenuItem("Sort", () =>
    actions.add(GridSortAction(gui.editPanel, () => gui.editPanel.sortAll)))
  gridMenu.add(sortItem)
  gridMenu.addSeparator

  val toggleShowAnchorsItem = MenuItemRef("Hide anchors", mi => {
    val p = gui.editPanel
    p.showAnchors = !p.showAnchors
    mi.setText(if (p.showAnchors) "Hide anchors" else "Show anchors")
    p.repaint()
  })
  gridMenu.add(toggleShowAnchorsItem)

  val toggleShowCentralZoneItem = MenuItemRef("Show central zone", mi => {
    val p = gui.editPanel
    p.showCentralZone = !p.showCentralZone
    mi.setText(if (p.showCentralZone) "Hide central zone" else "Show central zone")
    p.repaint()
  })
  gridMenu.add(toggleShowCentralZoneItem)

  var helpFrame: Option[HelpFrame] = None
  val helpItem = MenuItem("Help", () => {
    if (helpFrame.isEmpty) {
      val (w, h) = ((getParent.getWidth/3).toInt, (getParent.getHeight*0.75).toInt)
      helpFrame = Some(new HelpFrame(this, w, h))
    }
  })
  add(helpItem)
}
