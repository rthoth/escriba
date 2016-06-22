package test.filestore;

import io.escriba.store.FileStore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class FileStoreTest {

	private FileStore fileStore() {
		File dir;
		try {
			dir = File.createTempFile(this.getClass().getSimpleName(), "-FileStore");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		dir.mkdir();

		return new FileStore(dir);
	}

	@Test
	public void t01() {
		FileStore store = fileStore();

		store.collection("coll1").put();
	}
}
