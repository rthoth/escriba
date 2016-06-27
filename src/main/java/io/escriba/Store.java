package io.escriba;

import io.escriba.hash.HashCollection;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;
import java.util.zip.CRC32;

public class Store {

	private final WeakHashMap<String, HashCollection> collections = new WeakHashMap<>();
	private final File dataDir;
	private final DB db;

	public Store(File mapDBFile, File dataDir) {
		db = DBMaker.fileDB(mapDBFile)
			.transactionEnable()
			.make();

		this.dataDir = dataDir;
	}

	public Store(File dataDir) {
		db = DBMaker.memoryDB()
			.transactionEnable()
			.make();

		this.dataDir = dataDir;
	}

	public Collection collection(String collectionName) throws IOException {
		return collection(collectionName, false);
	}

	public Collection collection(String collectionName, boolean create) throws IOException {
		if (collections.containsKey(collectionName))
			return collections.get(collectionName);

		synchronized (this) {
			if (collections.containsKey(collectionName))
				return collections.get(collectionName);

			if (!db.exists(collectionName)) {
				if (create) {

					HTreeMap<String, Data> map = db.hashMap(collectionName, Serializer.STRING, Data.SERIALIZER).create();
					Atomic.Long atomic = db.atomicLong("atomic." + collectionName).create();
					collections.put(collectionName, new HashCollection(collectionName, map, getCollectionDataDir(collectionName), atomic));

				} else
					throw new IllegalStateException("Can't create " + collectionName + " collection!");
			} else {

				HTreeMap<String, Data> map = db.hashMap(collectionName, Serializer.STRING, Data.SERIALIZER).open();
				Atomic.Long atomic = db.atomicLong("atomic." + collectionName).open();
				collections.put(collectionName, new HashCollection(collectionName, map, getCollectionDataDir(collectionName), atomic));

			}

			return collections.get(collectionName);
		}
	}

	public static String collectionDirName(String collectionName) {
		CRC32 crc32 = new CRC32();
		crc32.update(collectionName.getBytes());
		return Long.toString(crc32.getValue(), 32);
	}

	@NotNull
	private File getCollectionDataDir(String collectionName) throws IOException {
		File collectionDir = new File(dataDir, collectionDirName(collectionName));
		try {
			collectionDir.mkdirs();
		} catch (Exception e) {
			throw new IOException("Impossible create " + collectionName + " collection!", e);
		}
		return collectionDir;
	}

}
