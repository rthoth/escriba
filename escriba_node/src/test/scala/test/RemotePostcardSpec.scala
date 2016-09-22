package test

import java.nio.ByteBuffer
import java.nio.charset.Charset

import io.escriba.DataEntry
import io.escriba.node.Node.NodeConfig
import io.escriba.node.{Action, Node, Postcard}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{AsyncFreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class RemotePostcardSpec extends AsyncFreeSpec with Matchers with TestServer with Implicits {

	lazy val node = new Node(server, new Node.NodeConfig(2))

	"A remote postcard should" - {

		val localPostcard = node.postcard("a-remote-postcard-spec")

		"write locally " in {
			var close = false

			node.put(localPostcard, "a-key", "text/plain", (0 until 200).mkString(","), (content: String) => {
				if (close)
					null
				else {
					val bytes = content.getBytes(Charset.forName("UTF-8"))
					val buffer = ByteBuffer.allocate(bytes.length)
					buffer.put(bytes).flip()
					close = true
					buffer
				}

			}).map(_ => server.store().collection("a-remote-postcard-spec", false).getEntry("a-key").size should be(689))
		}

		"read data correctly" in {
			val remotePostcard = new Postcard(localPostcard.collection, node.anchor)

			val aNode = new Node(newServer, new NodeConfig(2))

			var string = ""

			aNode.get(remotePostcard, "a-key", 50, (read: Long, entry: DataEntry, buffer: ByteBuffer) => {
				val b = Array.ofDim[Byte](buffer.limit())
				buffer.get(b)
				string += new String(b, Charset.forName("UTF-8"))
				if (read < entry.size) {
					Action.read[String](50)
				} else
					Action.stop(string)
			}).map(_ should be((0 until 200).mkString(",")))
		}
	}

}
