package tio

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

trait Runtime {
  def unsafeRunAsync[A](tio: TIO[A])(callback: Try[A] => Unit): Unit

  def unsafeRunSync[A](tio: TIO[A], timeout: Duration = Duration.Inf): Try[A] =
    Await.ready(unsafeRunToFuture(tio), timeout).value.get

  def unsafeRunToFuture[A](tio: TIO[A]): Future[A] = {
    val promise = Promise[A]()
    unsafeRunAsync(tio)(promise.tryComplete)
    promise.future
  }
}

object Runtime extends Runtime {
  private val executor = Executor.fixed(16, "tio-default")

  override def unsafeRunAsync[A](tio: TIO[A])(callback: Try[A] => Unit): Unit = {
    eval(tio)(callback.asInstanceOf[Try[Any] => Unit])
  }

  private def eval(tio: TIO[Any])(done: Try[Any] => Unit): Unit = {
    executor.submit {
      tio match {
        case TIO.Effect(a) =>
          done(Try(a()))

        case TIO.EffectAsync(callback) => callback(done)

        case TIO.FlatMap(tio, f: (Any => TIO[Any])) =>
          eval(tio) {
            case Success(res) => eval(f(res))(done)
            case Failure(e) => done(Failure(e))
          }

        case TIO.Fail(e) => done(Failure(e))

        case TIO.Recover(tio, f) =>
          eval(tio) {
            case Failure(e) => eval(f(e))(done)
            case success => done(success)
          }

        case TIO.EffectAsync(callback) =>
          callback(done)
      }
    }
  }
}

trait TIOApp {
  def run: TIO[Any]
  final def main(args: Array[String]): Unit = Runtime.unsafeRunSync(run).get
}

