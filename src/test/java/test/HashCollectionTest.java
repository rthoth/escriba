package test;

import io.escriba.Store;
import io.escriba.functional.Callback0;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class HashCollectionTest {

	@Test(timeOut = 1000L)
	public void insert01() throws Exception {
		Store store = new Store(Help.newFile("mapdb", this.getClass()), Help.newDir("data", this.getClass()));

		Wait wait = new Wait();

		Callback0 next = () -> store.collection("col", true).get("val", (read, close) -> wait.attempt(() -> {
				read.apply(ByteBuffer.allocate(1024 * 1024), 0L);
			}),

			(total, buffer, read, close) -> wait.attempt(() -> {
				assertThat(total).isEqualTo(1024 * 1024);
				close.apply();
				wait.free();
			})
		);

		store.collection("col", true).put("val", (write, close) -> wait.attempt(() -> {
				ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
				for (int i = 0; i < buffer.limit(); i++) {
					buffer.put((byte) i);
				}
				buffer.rewind();
				write.apply(buffer, 0L);

			}),

			(total, buffer, write, close) -> wait.attempt(() -> {
				assertThat(total).isEqualTo(1024 * 1024);
				close.apply();
				next.apply();
			})
		);

		wait.sleep();
	}


}
