package io.escriba.hash;

import io.escriba.*;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

/**
 * HashCollection uses HTreeMap.
 */
public class HashCollection implements Collection {
	private final Atomic.Long atomic;
	private final DataDirPool dataDirPool;
	final ExecutorService executor;
	final HTreeMap<String, DataEntry> map;
	final String name;

	public HashCollection(String name, DB db, DataDirPool dataDirPool, ExecutorService executor) {

		map = db.hashMap(name, Serializer.STRING, DataEntry.SERIALIZER).createOrOpen();

		this.name = name;
		this.executor = executor;

		atomic = db.atomicLong(name + ".nextID").createOrOpen();
		this.dataDirPool = dataDirPool;
	}

	@Override
	public Getter get(String key) {
		return new HashGetter(this, key);
	}

	DataEntry getEntry(String key) {
		return map.get(key);
	}

	DataEntry getOrCreateEntry(String key) {

		DataEntry entry = map.get(key);

		if (entry == null) {
			entry = new DataEntry().path(nextPath(), dataDirPool.next().index);
			map.put(key, entry);
		}

		return entry;
	}

	public Path getPath(String key) {
		DataEntry dataEntry = getOrCreateEntry(key);

		File dir = new File(dataDirPool.get(dataEntry.dataDirIndex).dir, Store.collectionDirName(name));

		if (!dir.exists())
			dir.mkdirs();

		return Paths.get(dir.getAbsolutePath(), dataEntry.path);
	}

	private String nextPath() {
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
