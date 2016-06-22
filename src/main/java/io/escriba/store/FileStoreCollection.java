package io.escriba.store;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class FileStoreCollection extends StoreCollection {
	private final File directory;
	private final String name;
	private final ReentrantLock collLock;
	private final IdGenerator collId;
	private final IdGenerator valId;

	public FileStoreCollection(File directory, String name, ReentrantLock collLock, IdGenerator collectionIdGen, IdGenerator valueIdGen) {
		this.directory = directory;
		this.name = name;
		this.collLock = collLock;
		this.collId = collectionIdGen;
		this.valId = valueIdGen;
	}
}
