package test

import java.nio.ByteBuffer
import java.nio.charset.Charset

import io.escriba.DataEntry
import io.escriba.node.ReadAction._
import io.escriba.node.{Node, WriteAction}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{AsyncFreeSpec, Matchers}

import scala.math.min

@RunWith(classOf[JUnitRunner])
class LocalPostcardSpec extends AsyncFreeSpec with Matchers with TestServer with Implicits {

	lazy val node = new Node(server, new Node.NodeConfig(2))

	"A local postcard should" - {
		val postcard = node.postcard("local-postcard-spec")

		"write any data correctly" in {

			//			var postcard = 0
			//
			//			postcard.put("akey", "text/plain", (0 until 100).mkString(","), )

			var buffer = ByteBuffer.allocate(0)

			node.put(postcard, "akey", "text/plain", 0, (0 until 100).mkString(","), (string: String, p: Int) => {

				val np = math.min(p + 2, string.length)
				val bytes = string.substring(p, np).getBytes(Charset.forName("UTF-8"))

				if (buffer.capacity() < bytes.length)
					buffer = ByteBuffer.allocate(bytes.length)

				buffer = (buffer before (_.rewind())).put(bytes) before {
					_.flip()
				}

				if (np < string.length)
					WriteAction.write(buffer, np)
				else
					WriteAction.stop[Int](buffer)

			}).map(_ => node.store.collection("local-postcard-spec", false).getEntry("akey").size should be(289))
		}

		"read data correctly" in {
			var string = ""

			node.get(postcard, "akey", 10, (total: Long, entry: DataEntry, buffer: ByteBuffer) => {
				val bytes = Array.ofDim[Byte](buffer.limit())
				buffer.get(bytes)
				string += new String(bytes, Charset.forName("UTF-8"))
				if (total < entry.size) {
					val np = min(total + 2, entry.size).toInt
					val r = np - total
					read[String](r.toInt)
				} else {
					stop(string)
				}
			}).map(_ should be((0 until 100).mkString(",")))
		}
	}

}
