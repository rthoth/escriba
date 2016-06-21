package io.escriba.store;

import java.io.File;

public class FileStore extends Store {

	private final File directory;

	public FileStore(File directory) {
		this.directory = directory;
	}
}
