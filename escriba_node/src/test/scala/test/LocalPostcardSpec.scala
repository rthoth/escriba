package test

import java.nio.ByteBuffer

import io.escriba.node.Node

class LocalPostcardSpec extends SpecLike with TestServer with Implicits {

	lazy val node = new Node(server)

	"A local postcard should " - {
		"write any data correctly" in {

			node
				.postcard("local-postcard-spec")
				.put("akey", "text/plain", "sample text", (string: String) => {
					val bytes = string.getBytes
					val buffer = ByteBuffer.allocate(bytes.length)
					buffer.put(bytes).flip()

					buffer
				})
		}
	}

}
