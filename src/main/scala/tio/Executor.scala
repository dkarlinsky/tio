package tio

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent._
import java.util.concurrent.Executors._

trait Executor {
  final def submit(thunk : => Unit): Unit = submitRunnable(() => thunk)
  def submitRunnable(thunk : Runnable): Unit
}

object Executor {
  private  val threadCounter = new AtomicInteger(0)
  private def nextThreadId = threadCounter.incrementAndGet()

  // creates Executor from a fixed thread pool, with named threads
  def fixed(threads: Int, namePrefix: String): Executor = {
    val executor = newFixedThreadPool(threads, namedDaemonThreads(namePrefix))
    thunk => executor.submit(thunk)
  }
  private def namedDaemonThreads(namePrefix: String): ThreadFactory = { thunk =>
    val thread = new Thread(thunk, s"$namePrefix-$nextThreadId")
    thread.setDaemon(true)
    thread.setUncaughtExceptionHandler((_, e) => e.printStackTrace())
    thread
  }
}
