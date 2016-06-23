package io.escriba.store.file;

import io.escriba.store.Get;
import io.escriba.store.IdGenerator;
import io.escriba.store.Put;
import io.escriba.store.StoreCollection;

import java.io.File;

public class FileStoreCollection extends StoreCollection {
	protected final File directory;
	protected final String name;
	protected final IdGenerator valueIdGen;

	public FileStoreCollection(File directory, String name, IdGenerator valueIdGen) {
		this.directory = directory;
		this.name = name;
		this.valueIdGen = valueIdGen;

		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	@Override
	public Get get(String key) {
		return null;
	}

	@Override
	public Put put(String key) {
		return new FilePut(this, key);
	}

}
