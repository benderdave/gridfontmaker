package farg

import java.awt.event.{KeyEvent, KeyListener, MouseEvent, MouseListener, 
 ItemListener, ItemEvent, ActionListener, ActionEvent}
import javax.swing.event.{ChangeEvent, ChangeListener, DocumentListener,
 DocumentEvent}

import javax.swing.{JPanel, SwingUtilities, JButton, ImageIcon, JMenuItem,
 JCheckBoxMenuItem, JCheckBox, JSlider, JTextField, JTextArea}
import javax.swing.SwingConstants.HORIZONTAL

// ---------------------------------------------------------------------------

object GUIUtils {

  def interpolate(dist: Double, startx: Double, starty: Double,
      endx: Double, endy: Double): (Double, Double) = {
    (startx + dist*(endx-startx), starty + dist*(endy-starty))
  }

  def Button(path: String, f: () => Any): JButton = {
    val icon = new ImageIcon(path)
    val button = new JButton(icon)
    button.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = f()
    })
    button
  }

  def MenuItem(name: String, f: () => Any): JMenuItem = {
    val item = new JMenuItem(name)
    item.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = f()
    })
    item
  }

  def MenuItemRef(name: String, f: (JMenuItem) => Any): JMenuItem = {
    val item = new JMenuItem(name)
    item.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = f(item)
    })
    item
  }

  def CheckBoxMenuItem(name: String, f: () => Any): JCheckBoxMenuItem = {
    val item = new JCheckBoxMenuItem(name)
    item.addItemListener(new ItemListener {
      override def itemStateChanged(e: ItemEvent): Unit = f()
    })
    item
  }

  def CheckBox(name: String, f: () => Any): JCheckBox = {
    val box = new JCheckBox(name)
    box.addItemListener(new ItemListener {
      override def itemStateChanged(e: ItemEvent): Unit = f()
    })
    box
  }

  def Slider(value: Double, f: () => Any): JSlider = {
    val slider = new JSlider(HORIZONTAL, 0, 100, (value*100).toInt)
    slider.addChangeListener(new ChangeListener {
      override def stateChanged(e: ChangeEvent): Unit = {
        val source = e.getSource().asInstanceOf[JSlider]
        if (source.getValueIsAdjusting) f()
      }
    })
    slider
  }

  def TextField(initialText: String, size: Int, f: (String) => Any): JTextField = {
    val text = new JTextField(size)
    text.setText(initialText)
    text.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        val source = e.getSource()
        if (source.isInstanceOf[JTextField]) f(text.getText())
      }
    })
    text.addKeyListener(new KeyListener {
      override def keyTyped(e: KeyEvent): Unit = {}
      override def keyPressed(e: KeyEvent): Unit = {}
      override def keyReleased(e: KeyEvent): Unit = f(text.getText())
    })
    text
  }

  def TextArea(initialText: String, rows: Int, cols: Int, f: (String) => Any): JTextArea = {
    val text = new JTextArea(initialText, rows, cols)
    text.setLineWrap(true)
    text.setWrapStyleWord(true)
    val docListener = new DocumentListener {
      override def insertUpdate(e: DocumentEvent): Unit = {
        val doc = text.getDocument()
        f(doc.getText(0, doc.getLength()))
      }
      override def removeUpdate(e: DocumentEvent): Unit = {
        val doc = text.getDocument()
        f(doc.getText(0, doc.getLength()))
      }
      override def changedUpdate(e: DocumentEvent): Unit = {}
    }
    text.getDocument().addDocumentListener(docListener)
    text.getDocument().putProperty("docListener", docListener)
    text
  }
}
