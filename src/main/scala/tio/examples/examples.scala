package tio.examples

import java.time.Instant
import java.util.{Random, Timer, TimerTask}

import scala.concurrent.duration._
import tio.{TIO, TIOApp}

import scala.concurrent.duration.Duration
import scala.util.Success
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

object Clock {
  // Use EffectAsync to implement a non-blocking "sleep"
  private val timer = new Timer("TIO-Timer", /* isDaemon */ true)

  def sleep[A](duration: Duration): TIO[Unit] =
    TIO.effectAsync { onComplete =>
      timer.schedule(new TimerTask {
        override def run(): Unit = onComplete(Success(()))
      }, duration.toMillis)
    }
}
import Clock._

object SleepExample extends TIOApp {
  def run = {
    for {
      _ <- TIO.effect(println(s"[${Instant.now}] running first effect on ${Thread.currentThread.getName}"))
      _ <- sleep(2.seconds)
      _ <- TIO.effect(println(s"[${Instant.now}] running second effect on ${Thread.currentThread.getName}"))
    } yield ()
  }
}

object ForkJoin extends TIOApp {
  def run = {
    for {
      _      <- putStrLn("1")
      fiber1 <- (sleep(2.seconds) *> putStrLn("2") *> TIO.succeed(1)).fork()
      _      <- putStrLn("3")
      i      <- fiber1.join()
      _      <- putStrLn(s"fiber1 done: $i")
    } yield ()
  }
}

object ForeachParExample extends TIOApp {
  val numbers = 1 to 10
  val random = new Random()
  // sleep up to 1 second, and return the duration slept
  val sleepRandomTime = TIO.effect(random.nextInt(1000).millis)
    .flatMap(t => sleep(t) *> TIO.succeed(t))
  def run = {
    for {
      _ <- putStrLn(s"[${Instant.now}] foreach:")
      _ <- TIO.foreach(numbers)(i => putStrLn(i.toString))
      _ <- putStrLn(s"[${Instant.now}] foreachPar:")
      _ <- TIO.foreachPar(numbers)(i =>
        sleepRandomTime.flatMap(t => putStrLn(s"$i after $t")))
      _ <- putStrLn(s"[${Instant.now}] foreachPar done")
    } yield ()
  }
}
