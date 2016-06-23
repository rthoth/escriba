package io.escriba.store.file;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

public abstract class FileValue {

	protected final FileStoreCollection collection;
	protected final File file;
	protected final String key;

	public FileValue(FileStoreCollection collection, String key) {
		this.collection = collection;
		this.key = key;

		this.file = new File(collection.directory, collection.valueIdGen.generate(key));
	}

	public AsynchronousFileChannel asyncChannel(OpenOption... options) throws IOException {
		for (OpenOption option : options)
			if (option == StandardOpenOption.WRITE) {
				File parent = file.getParentFile();
				if (parent.exists() == false) {
					parent.mkdirs();
				}
			}

		return AsynchronousFileChannel.open(file.toPath(), options);
	}
}
