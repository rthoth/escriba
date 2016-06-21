package io.escriba.store;

import java.io.File;

public class FileStore extends Store {

	private final File directory;
	private final FileStore config;

	public FileStore(File directory) {
		this(directory, FileStoreConfig.DEFAULT);
	}

	public FileStore(File directory, FileStore config) {
		this.directory = directory;
		this.config = config;
	}
}
