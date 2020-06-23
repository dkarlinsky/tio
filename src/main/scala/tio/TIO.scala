package tio

sealed trait TIO[+A] {
  def flatMap[B](f: A => TIO[B]): TIO[B] = TIO.FlatMap(this, f)
  def map[B](f: A => B): TIO[B] = flatMap(a => TIO.succeed(f(a)))
  // a convenience operator for sequencing effects, where the result of the
  // first effect is ignored
  def *> [B](that: TIO[B]): TIO[B] = flatMap(_ => that)
}

object TIO {
  // Initial algebra
  case class Effect[+A](a: () => A) extends TIO[A]
  // Effect combinator
  case class FlatMap[A, B](tio: TIO[A], f: A => TIO[B]) extends TIO[B]

  // Effect constructors
  def succeed[A](a: A): TIO[A] = Effect(() => a)
  def effect[A](a: => A): TIO[A] = Effect(() => a)
}

