package warden.driver

import java.io.File

object CLI {

  val parser = new scopt.OptionParser[Config]("warden") {
    head("warden", "0.x")

    opt[Unit]("debug") action { (_, c) =>
      c.copy(debug = true)
    } text ("log debug information")

    help("help") text ("prints this usage text")

    cmd("compile") action { (_, c) =>
      c.copy(mode = "update")
    } text ("compile gates") children(


        opt[File]('o', "out") valueName ("<path>") required() action { (x, c) =>
          c.copy(out = x)
        } text ("where to place the compiled jar"),
        arg[File]("<gate-file>...") unbounded() minOccurs(1) action { (x, c) =>
          c.copy(files = c.files :+ x)
        } text ("gate source files"),
        checkConfig { c =>
          success
        }
      )
  }

  def main(args: Array[String]): Unit = {
    parser.parse(args, Config()) match {
      case Some(config) =>
      // do stuff
        println(config)
      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

  final case class Config(
                           foo: Int = -1,
                           out: File = new File("."),
                           xyz: Boolean = false,
                           libName: String = "",
                           maxCount: Int = -1,
                           verbose: Boolean = false,
                           debug: Boolean = false,
                           mode: String = "",
                           files: Seq[File] = Seq.empty[File],
                           keepalive: Boolean = false,
                           jars: Seq[File] = Seq.empty[File],
                           kwargs: Map[String, String] = Map.empty[String, String]
                         )

}
