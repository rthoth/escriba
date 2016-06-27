package test;

import io.escriba.Close;
import io.escriba.Put;
import io.escriba.Store;
import io.escriba.Write;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class HashCollectionTest {

	@Test
	public void insert01() throws Exception {
		Store store = new Store(Help.newFile("mapdb", this.getClass()), Help.newDir("data", this.getClass()));

		store.collection("col", true).put("val").async(new Put.PutHandler() {
			@Override
			public void error(Throwable throwable) {
				// TODO: Mark as failed
			}

			@Override
			public void ready(Write write, Close close) {
				ByteBuffer buffer = ByteBuffer.allocate(1 << 20); // 1 << 20 = 2 ^ 20)
				byte b = 0;
				for (int i = 0; i < buffer.limit(); i++, b++) {
					buffer.put(b);
				}
				buffer.rewind();
				write.apply(buffer, 0L);
			}

			@Override
			public void written(int total, ByteBuffer buffer, Write write, Close close) {
				assertThat(total).isEqualTo(1 << 20);
			}
		});
	}
}
