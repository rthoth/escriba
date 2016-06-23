package test.filestore;

import io.escriba.store.Close;
import io.escriba.store.Put;
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
	public void writeAndRetrieve() {
		FileStore store = fileStore();

		StringBuilder sb = new StringBuilder();

		for (int i = 0, l = 2048 * 2048; i < l; i++) {
			sb.append(String.valueOf(i % 10));
		}

		final String str = sb.toString();
		final ByteBuffer[] buffer = {ByteBuffer.allocate(str.length())};
		buffer[0].put(str.getBytes()).rewind();

		store.collection("col").put("value")
			.onReady(new Put.ReadyCallback() {
				@Override
				public void apply(Put.Write write, Close close) throws Exception {
					write.apply(buffer[0], (long) 0);
				}
			})
			.onWrite(new Put.WriteCallback() {
				@Override
				public void apply(Integer writen, Put.Write write, Close close) throws Exception {
					if (buffer[0] != null) {
						buffer[0] = ByteBuffer.allocate(str.length());
						buffer[0].put(str.getBytes()).rewind();
						write.apply(buffer[0], (long) str.length());
						buffer[0] = null;
					} else
						close.apply();
				}
			})
			.onError(null)
			.start()
		;
	}
}
