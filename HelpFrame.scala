package farg

import java.io.IOException
import java.net.URL
import java.awt.Dimension
import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.{JFrame, SwingUtilities, JScrollPane, JEditorPane,
 ScrollPaneConstants}
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import javax.swing.border.EmptyBorder

// ----------------------------------------------------------------------------
class HelpFrame(val menu: GridfontMenuBar, width: Int, height: Int, 
    helpFile: URL) extends JFrame {
  setDefaultCloseOperation(DISPOSE_ON_CLOSE)
  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = menu.helpFrame = None
  })
  val scrollpane = new JScrollPane(new JEditorPane {
    setBorder(new EmptyBorder(50, 50, 50, 50))
    setEditable(false)
    try {
      setPage(helpFile)
    } catch {
      case e: IOException => println(s"can't open help file")
    }
  })
  scrollpane.setPreferredSize(new Dimension(width, height))
  scrollpane.setVerticalScrollBarPolicy(
    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
  setContentPane(scrollpane)
  SwingUtilities.invokeLater(new Runnable() {
    override def run(): Unit = {
      pack
      setVisible(true)
    }
  })
}
