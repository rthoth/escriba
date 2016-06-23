package io.escriba.store.file;

import io.escriba.store.Put;
import io.escriba.store.StoreException;

import java.io.IOException;

public class FilePut extends FileValue implements Put {
	public FilePut(FileStoreCollection collection, String key) {
		super(collection, key);
	}

	@Override
	public Put async(PutHandler handler) {
		try {
			new AsyncPut(this, handler);
		} catch (IOException e) {
			throw new StoreException(e);
		}
		return this;
	}
}
