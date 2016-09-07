package io.escriba.hash;

import io.escriba.*;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.nio.file.Files;
import java.nio.file.Path;
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

	@Override
	public DataEntry getEntry(String key) {
		return map.get(key);
	}

	DataEntry getOrCreateEntry(String key) {

		DataEntry entry = map.get(key);

		if (entry == null) {
			entry = DataEntry.DEFAULT
				.copy()
				.path(nextPath())
				.dataDirIndex(dataDirPool.nextIndex())
				.end()
			;
			map.put(key, entry);
		}

		return entry;
	}

	public Path getPath(String key) {
		DataEntry dataEntry = getOrCreateEntry(key);

		Path path = dataDirPool.get(dataEntry.dataDirIndex).path.resolve(Store.collectionDirName(name));

		if (!Files.exists(path))
			try {
				Files.createDirectories(path);
			} catch (Exception e) {
				throw new EscribaException.IllegalState("Impossible create " + path + " for collection " + name);
			}

		return path.resolve(dataEntry.path);
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

	@Override
	public Remover remove(String key) {
		return new HashRemover(this, key);
	}

	void removeEntry(String key) {
		map.remove(key);
	}

	void updateEntry(String key, DataEntry entry) {
		map.put(key, entry);
	}
}
