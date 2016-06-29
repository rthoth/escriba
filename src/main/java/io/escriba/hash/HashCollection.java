package io.escriba.hash;

import io.escriba.*;
import io.escriba.EscribaException.IllegalState;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HashCollection uses HTreeMap.
 */
public class HashCollection implements Collection {
	private final Atomic.Long atomic;
	private final DB db;
	private final File directory;
	final HTreeMap<String, DataEntry> map;
	final String name;

	public HashCollection(String name, DB db, File directory) {

		map = db.hashMap(name, Serializer.STRING, DataEntry.SERIALIZER).createOrOpen();

		this.name = name;
		this.directory = directory;
		this.db = db;

		this.atomic = db.atomicLong(name + ".nextID").createOrOpen();
	}

	@Override
	public DataChannel getChannel(String key) {
		if (this.map.containsKey(key)) {
			DataEntry entry = this.map.get(key);

			if (entry.status != DataEntry.Status.Ok)
				throw new IllegalState(key + " in collection!");

			return new FileDataChannel(entry, this.directory);
		}
		return null;
	}

	DataEntry getOrCreateEntry(String key) {

		DataEntry entry = map.get(key);

		if (entry == null) {
			entry = new DataEntry().path(nextPath());
			map.put(key, entry);
		}

		return entry;
	}

	Path getPath(DataEntry entry) {
		return Paths.get(directory.getAbsolutePath(), entry.path);
	}

	String nextPath() {
		synchronized (atomic) {
			return DataEntry.zyx(atomic.getAndIncrement());
		}
	}

	@Override
	public Putter put(String key, String mediaType) {
		return new HashPutter(this, key, mediaType);
	}

	void update(String key, DataEntry entry) {
		map.put(key, entry);
	}
}
