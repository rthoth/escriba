package test.put

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import io.escriba.{DataEntry, Getter, Putter}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import test.{Help, Spec}

@RunWith(classOf[JUnitRunner])
class BasicPutBehaviour extends Spec with Help {

	"Basic Put Behaviour " - {
		"simple put " - {
			val collection = store("simple-put").collection("simple-collection", true)
			val data = "123112312123132312kkkk5%%".getBytes()

			"report correctly written bytes" in {
				collection.put("a key", "text/plain").ready({ (control: Putter.Control) =>
					val buffer = ByteBuffer.allocate(1024 * 1024)

					buffer.put(data).limit(data.length).rewind()
					control.write(buffer)
				}).written({ (written: Int, buffer: ByteBuffer, control: Putter.Control) =>
					written should be(data.length)
					control.close()
				}).start().get(10, TimeUnit.SECONDS)
			}

			"record correctly bytes" in {

				collection.get("a key").ready({ (entry: DataEntry, control: Getter.Control) =>
					entry.size should be(data.length)

					control.read(ByteBuffer.allocate(1024))
				}).read({ (bytes: Int, buffer: ByteBuffer, control: Getter.Control) =>
					bytes should be(data.length)
					val array = Array.ofDim[Byte](bytes)
					buffer.rewind()
					buffer.get(array)
					new String(array) should be(new String(data))

					control.close()
				}).start().get(10, TimeUnit.SECONDS)
			}

		}
	}
}
