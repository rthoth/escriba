package io.escriba.store.file;

import io.escriba.store.IdGenerator;
import io.escriba.store.Store;
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
	public void get(String key, Store.Getter getter, Store.Fail fail) {
		new GetContext(this, key, getter, fail);
	}

	@Override
	public void put(String key, Store.Putter putter, Store.Fail fail) {
		new PutContext(this, key, putter, fail);
	}

}
