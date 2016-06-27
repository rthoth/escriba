package io.escriba.hash;

import io.escriba.Collection;
import io.escriba.Data;
import io.escriba.Put;
import org.mapdb.HTreeMap;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HashCollection implements Collection {

	private final File diretory;
	final HTreeMap<String, Data> map;
	private final String name;

	public HashCollection(String name, HTreeMap<String, Data> map, File directory) {
		this.name = name;
		this.map = map;
		this.diretory = directory;
	}

	public Path getPath(Data data) {
		return Paths.get(diretory.getAbsolutePath(), data.path);
	}

	@Override
	public Put put(String key) {
		return new HashPut(this, key);
	}
}
