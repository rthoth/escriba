package io.escriba.store.file;

import io.escriba.store.Get;
import io.escriba.store.StoreException;

public class FileGet extends FileValue implements Get {
	public FileGet(FileStoreCollection collection, String key) {
		super(collection, key);
	}

	@Override
	public Get async(GetHandler handler) {
		try {
			new AsyncGet(this, handler);
		} catch (Exception e) {
			throw new StoreException(e);
		}
		return null;
	}
}
