package tio

import scala.util.{Failure, Success, Try}

trait Runtime {
  // Run tio and return Success/Failure
  def unsafeRunSync[A](tio: TIO[A]): Try[A]
}

object Runtime extends Runtime {
  def unsafeRunSync[A](tio: TIO[A]): Try[A] = eval(tio)

  private def eval[A](tio: TIO[A]): Try[A] = {
    tio match {
      case TIO.Effect(a) =>
        Try(a())

      case TIO.FlatMap(tio, f: (Any => TIO[Any])) =>
        eval[Any](tio) match {
          case Success(res) => eval(f(res))
          case Failure(e) => Failure(e)
        }
    }
  }
}

trait TIOApp {
  def run: TIO[Any]
  final def main(args: Array[String]): Unit = Runtime.unsafeRunSync(run).get
}

