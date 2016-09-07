package test

import java.io.File
import java.io.File.separator
import java.nio.ByteBuffer
import java.util.Calendar
import java.util.concurrent.TimeUnit

import io.escriba.Putter.WrittenHandler
import io.escriba._

import scala.concurrent.duration.Duration
import scala.concurrent.{CanAwait, ExecutionContext, Future}
import scala.util.Try

trait Help {

	val base = {
		val c = Calendar.getInstance()

		import c.get

		val (yy, mm, dd) = (get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
		val (h, m, s) = (get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE), get(Calendar.SECOND))

		new File(s"build${separator}test-tmp${separator}${getClass.getName}-$yy-$mm-$dd-$h-$m-$s") before (_.mkdirs())
	}

	implicit def f2ErrorHandler(f: (Throwable) => Unit): ErrorHandler = if (f != null) new ErrorHandler {
		override def apply(throwable: Throwable): Unit = f(throwable)
	} else
		null

	implicit def f2GetterReadyHandler(f: (DataEntry, Getter.Control) => Unit): Getter.ReadyHandler = if (f != null) new Getter.ReadyHandler {
		override def apply(entry: DataEntry, control: Getter.Control): Unit = f(entry, control)
	} else
		null

	implicit def f2PutterReadyHandler(f: (Putter.Control) => Unit): Putter.ReadyHandler = if (f != null) new Putter.ReadyHandler {
		override def apply(control: Putter.Control): Unit = f(control)
	} else
		null

	implicit def f2PutterSuccessHandler(f: () => Unit): SuccessHandler = if (f != null) new SuccessHandler {
		override def apply(): Unit = f()
	} else
		null

	implicit def f2PutterWrittenHandler(f: (Int, ByteBuffer, Putter.Control) => Unit): Putter.WrittenHandler = if (f != null) new WrittenHandler {
		override def apply(written: Int, buffer: ByteBuffer, control: Putter.Control): Unit = f(written, buffer, control)
	} else
		null

	def newDir(name: String) = new File(base, name) before (_.mkdirs())

	def newFile(name: String) = new File(base, name)

	def store(name: String): Store = {
		new Store(newFile(name + "-store"), DataDir.of((Integer.valueOf(1), newDir(name + "-datadir"))), 1)
	}

	implicit def t2T2[A, B](t: (A, B)): T2[A, B] = T2.of(t._1, t._2)

	implicit class JFuture[T](future: java.util.concurrent.Future[T]) extends Future[T] {

		override def isCompleted: Boolean = future.isDone

		override def onComplete[U](f: (Try[T]) => U)(implicit executor: ExecutionContext): Unit = {
			executor.execute(new Runnable {
				override def run(): Unit = {
					if (future.isDone)
						f(Try(future.get()))
					else {
						Thread.sleep(50)
						executor.execute(this)
					}
				}
			})
		}

		override def ready(atMost: Duration)(implicit permit: CanAwait): JFuture.this.type = {
			future.get(atMost.toMillis, TimeUnit.MILLISECONDS)
			this
		}

		override def result(atMost: Duration)(implicit permit: CanAwait): T = {
			future.get(atMost.toMillis, TimeUnit.MILLISECONDS)
		}

		override def value: Option[Try[T]] = {
			if (future.isDone)
				Some(Try(future.get()))
			else
				None
		}
	}

	implicit class JFutureRich[T](future: java.util.concurrent.Future[T]) {
		def asScala: Future[T] = new JFuture(future)
	}

	implicit class Rich[A](any: A) {
		def before(f: A => Any): A = {
			f(any)
			any
		}
	}

}
