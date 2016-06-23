package test.filestore;

import io.escriba.store.Close;
import io.escriba.store.Get;
import io.escriba.store.Put;
import io.escriba.store.file.FileStore;
import org.assertj.core.api.Assertions;
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
	public void writeAndRetrieve() throws InterruptedException {
		FileStore store = fileStore();

		final StringBuilder sb = new StringBuilder();

		for (int i = 0, l = 2048 * 2048; i < l; i++) {
			sb.append(String.valueOf(i % 10));
		}

		final ByteBuffer buffer = ByteBuffer.allocate(sb.length());
		buffer.put(sb.toString().getBytes()).rewind();

		store.collection("col").put("value").async(new Put.PutHandler() {
			@Override
			public void data(int written, ByteBuffer buffer, Put.Write write, Close close) throws Exception {
				Assertions.assertThat(written).isEqualTo(sb.length());
				close.apply();
			}

			@Override
			public void error(Throwable throwable) throws Exception {
				throw new Exception("Ops!");
			}

			@Override
			public void ready(Put.Write write, Close close) throws Exception {
				write.apply(buffer, 0L);
			}
		});

		store.collection("col").get("value").async(new Get.GetHandler() {
			@Override
			public void data(int total, ByteBuffer buffer, Get.Read read, Close close) throws Exception {
				Assertions.assertThat(total).isEqualTo(sb.length());
				close.apply();
			}

			@Override
			public void error(Throwable throwable) throws Exception {

			}

			@Override
			public void ready(Get.Read read, Close close) throws Exception {
				read.apply(ByteBuffer.allocate(sb.length()), 0L);
			}
		});

		Thread.sleep(100000);
	}
}
