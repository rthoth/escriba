package test

import java.nio.ByteBuffer

import io.escriba.node.PostcardWriter

trait Implicits {

	implicit def f2PostcardWriter[T](f: (T) => ByteBuffer): PostcardWriter[T] = new PostcardWriter[T] {
		def apply(value: T): ByteBuffer = f(value)
	}
}
