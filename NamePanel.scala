package farg

import java.awt.{Color, Font=>AWTFont, FlowLayout}
import javax.swing.JPanel
import javax.swing.{SwingConstants, BorderFactory}

import GUIUtils.TextField

// ----------------------------------------------------------------------------
class NamePanel extends JPanel with GlobalFont with GlobalActionStack {
  import NamePanel._

  setBackground(Color.gray)
  setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0))
  val defaultText = "unnamed font by unknown author"
  val initialText = if (gfont.name == "") defaultText else gfont.name
  val nameField = TextField(initialText, 40, n => {
    val newName = n.filter(c => c.isLetterOrDigit || c.isSpaceChar || 
        Seq('_','-','.').contains(c))
    if (gfont.name != newName)
      setFontName(newName, false, false)
  })
  nameField.setBorder(BorderFactory.createEmptyBorder())
  nameField.setHorizontalAlignment(SwingConstants.CENTER)
  nameField.setFont(nameFont)
  nameField.setBackground(Color.gray)
  nameField.setForeground(Color.white)
  add(nameField)

  def setFontName(name: String, noChange: Boolean = false, 
      setTextFirstTime: Boolean = true): Unit = {
    val nameChange = NameChangeAction((firstTime: Boolean) => {
      if (!firstTime || setTextFirstTime)
        nameField.setText(name)
      gfont.name = name
      repaint()
    })
    if (noChange)
      nameChange.doit(true)
    else
      actions.add(nameChange)
  }

  def reset: Unit = setFontName(defaultText, true)
}

object NamePanel {
  val nameFont = new AWTFont("System", AWTFont.BOLD, 32)
}
