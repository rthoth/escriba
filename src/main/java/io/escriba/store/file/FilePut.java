package io.escriba.store.file;

import io.escriba.store.Put;

class FilePut extends Put implements FileContextable {

	private final Context context;

	public FilePut(FileStoreCollection collection, String key) {
		context = new Context(this, collection, key);
	}

	@Override
	public void ready() {

		if (readyCallback != null)
			readyCallback.apply();
	}

	@Override
	public void start() {
		context.lock();
	}
}
