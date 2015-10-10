package farg

import java.io.IOException
import java.net.URL
import java.awt.Dimension
import java.awt.event.{WindowListener, WindowEvent}
import javax.swing.{JFrame, SwingUtilities, JScrollPane, JEditorPane,
 ScrollPaneConstants}
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import javax.swing.border.EmptyBorder

// ----------------------------------------------------------------------------
class HelpFrame(val menu: GridfontMenuBar, width: Int, height: Int) 
    extends JFrame with WindowListener {
  setDefaultCloseOperation(DISPOSE_ON_CLOSE)
  addWindowListener(this)
  val scrollpane = new JScrollPane(new JEditorPane {
    setBorder(new EmptyBorder(50, 50, 50, 50))
    setEditable(false)
    val helpFile = new URL("file:./help.html")
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
  override def windowClosing(e: WindowEvent): Unit = menu.helpFrame = None
  override def windowActivated(e: WindowEvent): Unit = {}
  override def windowClosed(e: WindowEvent): Unit = {}
  override def windowDeactivated(e: WindowEvent): Unit = {}
  override def windowIconified(e: WindowEvent): Unit = {}
  override def windowDeiconified(e: WindowEvent): Unit = {}
  override def windowOpened(e: WindowEvent): Unit = {}
}
