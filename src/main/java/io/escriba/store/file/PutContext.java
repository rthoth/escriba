package io.escriba.store.file;

import io.escriba.functional.Callback2;
import io.escriba.store.Store;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;

class PutContext extends Context<Integer> {
	private FileStoreCollection collection;
	private final Writer writer;

	public PutContext(FileStoreCollection collection, String key, Store.Putter putter, Store.Fail fail) {
		super(collection, key, putter, fail);
		this.collection = collection;

		writer = new Writer(this);

		if (closed == false)
			channel.lock(null, new GetLocker(this));
	}

	@Override
	protected Callback2<ByteBuffer, Long> callback() {
		return writer;
	}

	@Override
	protected AsynchronousFileChannel openChannel(File file) throws IOException {
		return AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}
}
