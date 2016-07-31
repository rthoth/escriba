package test.put

import java.nio.ByteBuffer

import io.escriba.{DataEntry, Putter}
import org.junit.runner.RunWith
import org.specs2.Specification
import org.specs2.runner.JUnitRunner
import test.Help

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionException}

@RunWith(classOf[JUnitRunner])
class BasicPutBehaviour extends Specification with Help {

	val readyNOP = (control: Putter.Control) => {
		control.write(ByteBuffer.allocate(1))
	}

	val writtenNOP = (w: Int, b: ByteBuffer, c: Putter.Control) => {
		c.close()
	}

	//noinspection SpellCheckingInspection
	def is =
	s2"""
Basic put behaviour:
	1kb data bytes									${putZeros(1024).size === 1024}
	128kb data bytes								${putZeros(128 * 1024).size === (128 * 1024)}
	8mb data bytes									${putZeros(8 * 1024 * 1024).size === (8 * 1024 * 1024)}
	512mb data bytes								${putZeros(512 * 1024 * 1024).size === (512 * 1024 * 1024)}
	Status ok!										${putZeros(1025).status === DataEntry.Status.Ok}

Sometimes something wrong happen:
	future throws ready error					${put("ftre")(written = writtenNOP, ready = (c) => throw new IllegalArgumentException("asd")) must throwAn[IllegalArgumentException]("asd")}
	future throws written error				${put("ftwe")(ready = readyNOP, written = (w, b, c) => throw new IllegalStateException("dsa")) must throwAn[IllegalStateException]("dsa")}
	future throws error error					${put("ftee")(ready = readyNOP, written = writtenNOP, error = (t) => throw new NullPointerException("rfv")) must not throwA[Throwable]()}
	Status creating!								${put("ftre1", throwss = false)(ready = readyNOP, written = (w, b, c) => throw new IllegalStateException("asdf")).status === DataEntry.Status.Creating}
"""

	//noinspection SpellCheckingInspection
	def put(storeName: String, key: String = "key", mediaType: String = "text/plain", throwss: Boolean = true)(
		ready: (Putter.Control) => Unit = null,
		written: (Int, ByteBuffer, Putter.Control) => Unit = null,
		error: (Throwable) => Unit = null,
		success: () => Unit = null
	) = {

		val s = store(storeName)

		val future = s
			.collection("coll", true)
			.put(key, mediaType)
			.ready(ready)
			.written(written)
			.error(error)
			.success(success)
			.apply()

		try
			Await.result(future, 1.minute)
		catch {
			case e: ExecutionException =>
				if (throwss)
					throw e.getCause
				else {
					s.collection("coll", false).getEntry(key)
				}
		}
	}

	def putZeros(length: Int) = {

		var current = 0

		import scala.math._

		put(s"putZeros-$length", "some-value", "text/plain")(

			ready = (control) => {
				control.write(ByteBuffer.allocate(min(1024 * 512, length)))
			},

			written = (written, buffer, control) => {
				current += written

				if (current >= length)
					control.close()
				else {
					buffer.rewind()

					buffer.limit(min(1024 * 512, length - current))
					control.write(buffer)
				}
			},

			error = (error) => {

			},

			success = () => {

			}
		)
	}

	def readyErrors = {
		put("coll-ready", "k", "text/plain")(
			ready = (control) => {
				throw new IllegalStateException("010")
			}
		) must throwAn[IllegalStateException]("010")
	}

	def writtenErrors = {
		put("written", "k", "media")(
			ready = (control) => {
				control.write(ByteBuffer.allocate(1))
			},
			written = (len, buffer, control) => {
				throw new NullPointerException("zxcasd")
			}
		) must throwA[NullPointerException]("zxcasd")
	}
}
