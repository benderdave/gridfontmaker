package farg

import scala.collection.mutable.Map
import com.owlike.genson.defaultGenson._
import java.io.PrintWriter

// ----------------------------------------------------------------------------
case class Font(var name: String = "", letters: Map[String,Letter] = Map.empty) {
  val alphabet = 'a' to 'z'

  // verify letters
  for ((ch, letter) <- letters)
    if (!alphabet.contains(ch.head))
      throw new Exception("bad letter")

  // add any missing letters
  for (ch <- alphabet.toSet diff letters.map(_._2.ch.head).toSet)
    letters(ch.toString) = Letter(ch.toString)

  var example_text = "woven silk pyjamas\nexchanged for blue quartz"

  def save(saveName: String): String = {
    val currentState = toJson(this)
    println(s"writing $saveName")
    new PrintWriter(s"$saveName") {
      write(currentState)
      close 
    }
    currentState
  }

  def clear: Unit = {
    name = ""
    example_text = ""
    letters.foreach(_._2.clear)
  }
}
