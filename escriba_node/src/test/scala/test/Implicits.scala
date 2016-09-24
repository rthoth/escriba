package test

import java.nio.ByteBuffer
import java.util.concurrent.{ExecutionException, TimeUnit, TimeoutException}

import io.escriba.DataEntry
import io.escriba.node._

import scala.concurrent.duration.Duration
import scala.concurrent.{CanAwait, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait Implicits {

	implicit def f2PostcardReader[T](f: (Long, DataEntry, ByteBuffer) => ReadAction[T]): PostcardReader[T] = new PostcardReader[T] {
		override def apply(total: Long, entry: DataEntry, buffer: ByteBuffer): ReadAction[T] = f(total, entry, buffer)
	}

	implicit def f2PostcardReader0[T](f: (DataEntry, ByteBuffer) => T): PostcardReader0[T] = new PostcardReader0[T] {
		override def apply(entry: DataEntry, buffer: ByteBuffer): T = f(entry, buffer)
	}

	implicit def f2PostcardWriter[T, P](f: (T, P) => WriteAction[P]): PostcardWriter[T, P] = new PostcardWriter[T, P] {
		override def apply(value: T, previous: P): WriteAction[P] = f(value, previous)
	}

	implicit def f2PostcardWriter0[T](f: (T) => ByteBuffer): PostcardWriter0[T] = new PostcardWriter0[T] {
		override def apply(value: T): ByteBuffer = f(value)
	}

	implicit class Before[T](x: T) {
		def before[R](y: T => R): T = {
			y(x)
			x
		}
	}

	implicit class SFuture[T](future: java.util.concurrent.Future[T]) extends Future[T] {
		override def isCompleted: Boolean = ???

		override def onComplete[U](f: (Try[T]) => U)(implicit executor: ExecutionContext): Unit = {
			executor.execute(new Runnable {
				override def run(): Unit = {
					if (future.isDone)
						try {
							f(Success(future.get()))
						} catch {
							case ex: ExecutionException =>
								f(Failure(ex.getCause))
							case t: Throwable =>
								f(Failure(t))
						}
					else {
						Thread.sleep(50)
						executor.execute(this)
					}
				}
			})
		}

		@scala.throws[InterruptedException](classOf[InterruptedException])
		@scala.throws[TimeoutException](classOf[TimeoutException])
		override def ready(atMost: Duration)(implicit permit: CanAwait): SFuture.this.type = {
			Thread.sleep(atMost.toMillis)
			this
		}

		@scala.throws[Exception](classOf[Exception])
		override def result(atMost: Duration)(implicit permit: CanAwait): T = {
			future.get(atMost.toMillis, TimeUnit.MILLISECONDS)
		}

		override def value: Option[Try[T]] = Some(Try(future.get()))
	}

}
