package io.escriba.store.file;

import io.escriba.store.IdGenerator;
import io.escriba.store.Store;
import io.escriba.store.StoreCollection;

import java.io.File;

public class FileStore extends Store {

	private final IdGenerator collIdGen;
	private final FileStoreConfig config;
	private final File directory;
	private final IdGenerator valueIdGen;

	public FileStore(File directory) {
		this(directory, FileStoreConfig.DEFAULT);
	}

	public FileStore(File directory, FileStoreConfig config) {
		directory.mkdirs();
		this.directory = directory;
		this.config = config;
		this.collIdGen = new IdGenerator(config.colIdGenParts);
		this.valueIdGen = new IdGenerator(config.valIdGenParts);
	}

	@Override
	public StoreCollection collection(String name) {
		return new FileStoreCollection(new File(directory, collIdGen.generate(name)), name, valueIdGen);
	}
}
