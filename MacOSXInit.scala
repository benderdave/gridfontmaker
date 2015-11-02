package farg

import javax.swing.{JFrame, JOptionPane}

// ----------------------------------------------------------------------------
class MacOSXInit extends PlatformSpecificInit {
  override def initialize(frame: JFrame): Unit = {
    import com.apple.eawt._
    import com.apple.eawt.AppEvent._

    val macApplication = Application.getApplication
     
    macApplication.setAboutHandler(new AboutHandler {
      def handleAbout(e: AboutEvent) {
        JOptionPane.showMessageDialog(frame,
          "Version 1.3\nWritten by Dave Bender\nFluid Analogies Research Group (FARG)\nIndiana University",
          "GridfontMaker",
          JOptionPane.PLAIN_MESSAGE)
      }
    })
  }
}
