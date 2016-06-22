package io.escriba.store;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class FileStore extends Store {

	private final File directory;
	private final FileStoreConfig config;
	private final IdGenerator collectionIdGen;
	private final IdGenerator valueIdGen;

	private final ReentrantLock collLock = new ReentrantLock();

	public FileStore(File directory) {
		this(directory, FileStoreConfig.DEFAULT);
	}

	public FileStore(File directory, FileStoreConfig config) {
		this.directory = directory;
		this.config = config;
		this.collectionIdGen = new IdGenerator(config.collectionIdGenParts);
		this.valueIdGen = new IdGenerator(config.valueIdGenParts);
	}

	@Override
	public StoreCollection collection(String name) {
		return new FileStoreCollection(directory, name, collLock, collectionIdGen, valueIdGen);
	}
}
