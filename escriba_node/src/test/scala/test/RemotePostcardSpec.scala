package test

import java.nio.ByteBuffer
import java.nio.charset.Charset

import io.escriba.DataEntry
import io.escriba.node.{Node, Postcard, ReadAction, WriteAction}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{AsyncFreeSpec, Matchers}

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class RemotePostcardSpec extends AsyncFreeSpec with Matchers with TestServer with Implicits {

	lazy val localServer = new Node(newServer, new Node.NodeConfig(2))

	lazy val remoteServer = new Node(server, new Node.NodeConfig(2))

	"A remote postcard should" - {

		val localPostcard = remoteServer.postcard("a-remote-postcard-spec")

		"write locally" - {

			"correctly" in {
				remoteServer.put(localPostcard, "a-key", "text/plain", null, (0 until 200).mkString(","), (content: String, n: Null) => {
					val bytes = content.getBytes(Charset.forName("UTF-8"))
					val buffer = ByteBuffer.allocate(bytes.length)
					buffer.put(bytes).flip()
					WriteAction.stop[Null](buffer)
				}).map(_ => server.store().collection("a-remote-postcard-spec", false).getEntry("a-key").size should be(689))
			}

			"and read remotely" in {
				val remotePostcard = new Postcard(localPostcard.collection, remoteServer.anchor)


				var string = ""

				localServer.get(remotePostcard, "a-key", 50, (read: Long, entry: DataEntry, buffer: ByteBuffer) => {
					val b = Array.ofDim[Byte](buffer.limit())
					buffer.get(b)
					string += new String(b, Charset.forName("UTF-8"))
					if (read < entry.size) {
						ReadAction.read[String](50)
					} else
						ReadAction.stop(string)
				}).map(_ should be((0 until 200).mkString(",")))
			}
		}

		"write remotely" - {

			val collection = "a-collection-remote-1"

			"correctly" in {
				val remotePostcard = new Postcard(collection, remoteServer.anchor)
				val buf = ByteBuffer.allocate(2)

				localServer.put(remotePostcard, "akkey", "text/plain", 0, (1 to 213).mkString(":"), (string: String, i: Int) => {
					buf.rewind()

					val ni = i + 1
					buf.put(string.substring(i, ni).getBytes(Charset.forName("UTF-8"))).flip()

					if (ni < string.length)
						WriteAction.write(buf, ni)
					else
						WriteAction.stop[Int](buf)
				}).map(_ => {
					remoteServer.store.collection(collection, false).getEntry("akkey").size should be(743)
				})
			}

			"and read correctly" in {
				val remotePostcard = new Postcard(collection, remoteServer.anchor)

				var string = ""

				localServer.get(remotePostcard, "akkey", 30, (total: Long, entry: DataEntry, buffer: ByteBuffer) => {
					val bytes = Array.ofDim[Byte](buffer.limit())
					buffer.get(bytes)

					string += new String(bytes, Charset.forName("UTF-8"))

					if (total < entry.size)
						ReadAction.read[String](10 + Random.nextInt(40))
					else
						ReadAction.stop(string)
				}).map(_ should be((1 to 213).mkString(":")))
			}
		}

	}

}
