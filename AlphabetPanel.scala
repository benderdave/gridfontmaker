package farg

import java.awt.Color
import javax.swing.{JPanel, BoxLayout}
import javax.swing.border.EmptyBorder

// ----------------------------------------------------------------------------
class AlphabetPanel extends JPanel with GlobalFont {
  setBackground(Color.gray)
  val textPanel = new GridfontTextArea(9.0, ('a' to 'z').mkString)
  add(textPanel)
}

// ----------------------------------------------------------------------------
class AlphabetAndNameArea extends JPanel with GlobalFont {
  setBackground(Color.gray)
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new EmptyBorder(0, 0, 0, 0))
  val namePanel = new NamePanel
  add(namePanel)
  val alphPanel = new AlphabetPanel
  add(alphPanel)
  def clear: Unit = namePanel.reset
}
