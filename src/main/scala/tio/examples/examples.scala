package tio.examples

import tio.{TIO, TIOApp}

import scala.util.control.NonFatal

object SequenceEffects extends TIOApp {
  def run = {
    for {
      _ <- TIO.effect(println("running first effect"))
      _ <- TIO.effect(println("running second effect"))
    } yield ()
  }
}

object Console {
  def putStrLn(str: => String) = TIO.effect(println(str))
}
import Console._

object ExampleWithThrow extends TIOApp {
  override def run = {
    for {
      _ <- putStrLn("running first effect")
      _ <- TIO.effect(throw new RuntimeException)
      _ <- putStrLn("running second effect")
    } yield ()
  }
}

object FailAndRecover extends TIOApp {
  def run = {
    (for {
      _ <- putStrLn("running first effect")
      _ <- TIO.fail(new RuntimeException)
      _ <- putStrLn("second effect - will not run")
    } yield ()).recover {
      case NonFatal(e) =>
        putStrLn(s"recovered from failure: ${e.getClass.getName}")
    }
  }
}

object Foreach10k extends TIOApp {
  def run = TIO.foreach(1 to 10000)(i => TIO.effect(println(i)))
}
