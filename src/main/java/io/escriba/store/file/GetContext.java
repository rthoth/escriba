package io.escriba.store.file;

import io.escriba.functional.Callback2;
import io.escriba.functional.T2;
import io.escriba.store.Store;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;

class GetContext extends Context<T2<Integer, ByteBuffer>> {

	private final Reader reader;

	public GetContext(FileStoreCollection collection, String key, Store.Getter getter, Store.Fail fail) {
		super(collection, key, getter, fail);
		reader = new Reader(this);
		try {
			callUserCode(null);
		} catch (Exception e) {
			callFail(e);
		}
	}

	@Override
	protected Callback2<ByteBuffer, Long> callback() {
		return reader;
	}

	@Override
	protected AsynchronousFileChannel openChannel(File file) throws IOException {
		return AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
	}
}
