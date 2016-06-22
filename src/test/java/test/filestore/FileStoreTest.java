package test.filestore;

import io.escriba.functional.Callback0;
import io.escriba.functional.Callback2;
import io.escriba.functional.T2;
import io.escriba.store.Store;
import io.escriba.store.file.FileStore;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class FileStoreTest {

	private FileStore fileStore() {
		File dir;
		dir = new File(String.format("build%sFile-Store-%s", File.separator, Long.toHexString(new Date().getTime())));
		dir.mkdirs();

		return new FileStore(dir);
	}

	@Test
	public void writeAndRetrieve() {
		FileStore store = fileStore();

		final String s1 = "A test", s2 = " ok!";


		store.collection("coll1").put("value", new Store.Putter() {
			@Override
			public void apply(Integer writen, Callback2<ByteBuffer, Long> write, Callback0 close) throws Exception {
				if (writen == null) {
					ByteBuffer buffer = ByteBuffer.allocate(s1.length());
					buffer.put(s1.getBytes()).rewind();
					write.apply(buffer, (long) 0);

				} else if (writen == s1.length()) {
					ByteBuffer buffer = ByteBuffer.allocate(s2.length());
					buffer.put(s2.getBytes()).rewind();
					write.apply(buffer, (long) s1.length());

				} else {
					close.apply();
				}
			}
		});

		store.collection("coll1").get("value", new Store.Getter() {
			@Override
			public void apply(T2<Integer, ByteBuffer> last, Callback2<ByteBuffer, Long> read, Callback0 close) throws Exception {
				if (last == null) {
					read.apply(ByteBuffer.allocate(10), (long) 0);

				} else if (last.a == 10) {
					String value = new String(last.b.array());
					assertThat(value).isEqualTo(s1.concat(s2));

				} else {
					close.apply();
				}
			}
		});
	}
}
