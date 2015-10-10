package farg

import scala.language.existentials

case class CommandLineParser(opts: OptionalArg*) {
  def parse(args: List[String]): Boolean = {
    case class Switch(switch: String, value: String, var done: Boolean = false)

    def preparse(args: List[String], switches: List[Switch]): List[Switch] = {
      args match {
        case Nil => switches
        case name :: Nil => List(Switch(name, ""))
        case name :: value :: more =>
          preparse(more, Switch(name, value) :: switches)
      }
    }

    val switches = preparse(args, List())
    for (opt <- opts) {
      switches.find(_.switch == opt.switch) match {
        case Some(sw) => opt.run(sw.value); sw.done = true
        case None => opt.runDefault()
      }
    }

    switches.filter(!_.done).foreach(switch =>
      System.err.println("unknown switch " + switch.switch)
    )
    true
  }
}

case class CommandLineArgError(message: String) extends Exception

case class OptionalArg(switch: String, default: Any, action: _ => Unit) {
  def toListOfInt(s: String): List[Int] = s.split(",").map(_.toInt).toList

  def run(value: String): Unit = {
    try {
      default match {
        case _: String => action.asInstanceOf[String => Unit](value)
        case _: Int => action.asInstanceOf[Int => Unit](value.toInt)
        case _: Double => action.asInstanceOf[Double => Unit](value.toDouble)
        case _: List[_] => action.asInstanceOf[List[Int] => Unit](
          toListOfInt(value)
        )
      }
    } catch {
      case ex: CommandLineArgError => System.err.println(ex.message); System.exit(-1) // FIXME: Probably shouldn't exit; better way?
    }
  }

  def runDefault(): Unit = {
    try {
      default match {
        case d: String => action.asInstanceOf[String => Unit](d)
        case d: Int => action.asInstanceOf[Int => Unit](d)
        case d: Double => action.asInstanceOf[Double => Unit](d)
        case d: List[_] => action.asInstanceOf[List[Int] => Unit](
          d.asInstanceOf[List[Int]]
        )
      }
    } catch {
      case ex: CommandLineArgError => System.err.println(ex.message); System.exit(-1) // FIXME: Probably shouldn't exit; better way?
    }
  }
}
