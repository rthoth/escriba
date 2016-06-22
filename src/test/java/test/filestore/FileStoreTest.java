package test.filestore;

import io.escriba.Func;
import io.escriba.store.Store;
import io.escriba.store.file.FileStore;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;

public class FileStoreTest {

	private FileStore fileStore() {
		File dir;
		dir = new File(String.format("build%sFile-Store-%s", File.separator, Long.toHexString(new Date().getTime())));
		dir.mkdirs();

		return new FileStore(dir);
	}

	@Test
	public void t01() {
		FileStore store = fileStore();

		final String s1 = "A test", s2 = " ok!";

		store.collection("coll1").put("value", new Store.Putter() {
			@Override
			public void apply(Integer writen, Func.C2<ByteBuffer, Long> write, Func.C0 close) throws Exception {
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
	}
}
