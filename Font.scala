package farg

import scala.collection.mutable.Map
import com.owlike.genson.defaultGenson._
import java.io.PrintWriter

// ----------------------------------------------------------------------------
class IllegalLetterException extends Exception

case class Font(var name: String = "", letters: Map[String,Letter] = Map.empty) {
  import GridfontMaker.fullAlphabet

  // verify letters
  for ((ch, letter) <- letters) {
    if (!fullAlphabet.contains(ch.head)) {
      println(s"ERROR: illegal letter '$ch'")
      throw new IllegalLetterException
    }
  }

  // add any missing letters
  for (ch <- fullAlphabet.toSet diff letters.map(_._2.ch.head).toSet)
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
}
