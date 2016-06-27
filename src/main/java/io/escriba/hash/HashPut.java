package io.escriba.hash;

import io.escriba.Data;
import io.escriba.Put;

import java.nio.file.Path;

public class HashPut implements Put {
	private final HashCollection collection;
	protected final String key;

	public HashPut(HashCollection collection, String key) {
		this.key = key;
		this.collection = collection;
	}

	@Override
	public Put async(PutHandler handler) throws Exception {
		new HashPutHashAsync(this, handler);
		return this;
	}

	public Data data() {
		Data data = collection.map.get(key);
		if (data != null)
			return data;
		else
			return new Data();
	}

	public Path getPath(Data data) {
		return collection.getPath(data);
	}
}
