package tio

import tio.TIO.AsyncDoneCallback

import scala.util.Try


sealed trait TIO[+A] {
  def flatMap[B](f: A => TIO[B]): TIO[B] = TIO.FlatMap(this, f)
  def map[B](f: A => B): TIO[B] = flatMap(a => TIO.succeed(f(a)))
  def recover[B >: A](f: Throwable => TIO[B]): TIO[B] = TIO.Recover(this, f)
  def fork(): TIO[Fiber[A]] = TIO.Fork(this)
  // a convenience operator for sequencing effects, where the result of the
  // first effect is ignored
  def *> [B](that: TIO[B]): TIO[B] = flatMap(_ => that)
}

object TIO {
  type AsyncDoneCallback[T] = Try[T] => Unit
  type AsyncTask[T] = AsyncDoneCallback[T] => Unit

  // Initial algebra
  case class Effect[+A](a: () => A) extends TIO[A]
  case class Fail[A](e: Throwable) extends TIO[A]
  case class EffectAsync[+A](asyncTask: AsyncTask[A]) extends TIO[A]
  // Effect combinators
  case class FlatMap[A, B](tio: TIO[A], f: A => TIO[B]) extends TIO[B]
  case class Recover[A](tio: TIO[A], f: Throwable => TIO[A]) extends TIO[A]
  // Fiber related effects
  case class Fork[A](tio: TIO[A]) extends TIO[Fiber[A]]
  case class Join[A](fiber: Fiber[A]) extends TIO[A]


  // Effect constructors
  def succeed[A](a: A): TIO[A] = Effect(() => a)
  def effect[A](a: => A): TIO[A] = Effect(() => a)
  def fail[A](throwable: Throwable): TIO[A] = Fail(throwable)
  def effectAsync[A](asyncTask: AsyncTask[A]): TIO[A] = EffectAsync(asyncTask)


  def foreach[A, B](xs: Iterable[A])(f: A => TIO[B]): TIO[Iterable[B]] =
    xs.foldLeft(TIO.succeed(Vector.empty[B]))((acc, curr) => for {
      soFar <- acc
      x <- f(curr)
    } yield soFar :+ x)

  def foreachPar[A, B](xs: Iterable[A])(f: A => TIO[B]): TIO[Iterable[B]] = {
    foreach(xs)(x => f(x).fork()).flatMap( fibers => foreach(fibers)(_.join()))
  }
}

trait Fiber[+A] {
  def join(): TIO[A] = TIO.Join(this)
  // called internally by the runtime
  private [tio] def onDone(done: AsyncDoneCallback[Any]): Fiber[A]
}
