package tio.examples

import tio.{TIO, TIOApp}

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
