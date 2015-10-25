package farg

import java.awt.{Color, BorderLayout}
import java.awt.event.{MouseAdapter, MouseEvent}
import javax.swing.{JPanel, BoxLayout}
import javax.swing.border.EmptyBorder

import GUIUtils.Button

// ----------------------------------------------------------------------------
class AlphabetPanel extends JPanel with GlobalFont {
  setBackground(Color.gray)
  val textArea = new GridfontTextArea(() => gfont, 14.0,
    GridfontMaker.fullAlphabetString, false, true)
  add(textArea)
}

// ----------------------------------------------------------------------------
class CompareAlphabetPanel(font: Font) extends JPanel {
  setBackground(Color.gray)
  setLayout(new BorderLayout)

  val closeButton = Button("close-icon.png", () => {
    val p = getParent()
    if (p != null) {
      p.remove(this)
      p.revalidate()
    }
  })
  closeButton.setBackground(Color.gray)
  closeButton.setBorder(new EmptyBorder(10, 10, 10, 10))
  closeButton.setEnabled(false)
  setFocusable(true)

  add(closeButton, BorderLayout.WEST)
  val textArea = new GridfontTextArea(() => font, 14.0,
    GridfontMaker.fullAlphabetString, false, true)
  add(textArea, BorderLayout.CENTER)

  val closer = new MouseAdapter {
    override def mouseEntered(e: MouseEvent): Unit =
      closeButton.setEnabled(true)
    override def mouseExited(e: MouseEvent): Unit =
      closeButton.setEnabled(false)
  }
  addMouseListener(closer)
  closeButton.addMouseListener(closer)
  textArea.addMouseListener(closer)
}

// ----------------------------------------------------------------------------
class AlphabetAndNameArea extends JPanel {
  setBackground(Color.gray)
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new EmptyBorder(0, 0, 0, 0))
  setFocusable(true)

  val namePanel = new NamePanel
  add(namePanel)
  val alphPanel = new AlphabetPanel
  add(alphPanel)

  def addCompare(compareFont: Font): Unit = {
    add(new CompareAlphabetPanel(compareFont))
    revalidate()
  }

  def clear: Unit = {
    namePanel.reset
    val comparePanels = getComponents.collect {
      case panel: CompareAlphabetPanel => panel
    }
    if (comparePanels.nonEmpty) {
      comparePanels.foreach(remove(_))
      revalidate()
    }
  }
}
