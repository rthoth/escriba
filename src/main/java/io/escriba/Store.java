package io.escriba;

import io.escriba.EscribaException.NotFound;
import io.escriba.EscribaException.Unexpected;
import io.escriba.hash.HashCollection;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;
import java.util.zip.CRC32;

public class Store {

	private final File baseDir;
	private final WeakHashMap<String, HashCollection> collections = new WeakHashMap<>();
	private final DB db;

	public Store(File mapDBFile, File baseDir) {
		this.db = DBMaker.fileDB(mapDBFile)
			.transactionEnable()
			.make();

		this.baseDir = baseDir;
	}

	public Store(File baseDir) {
		this.db = DBMaker.memoryDB()
			.transactionEnable()
			.make();

		this.baseDir = baseDir;
	}

	public Collection collection(String collectionName, boolean create) throws Exception {
		if (this.collections.containsKey(collectionName))
			return this.collections.get(collectionName);

		synchronized (this) {
			if (this.collections.containsKey(collectionName))
				return this.collections.get(collectionName);

			if (create || this.db.exists(collectionName))
				try {
					this.collections.put(collectionName, new HashCollection(collectionName, this.db, this.directoryOf(collectionName)));
				} catch (Exception e) {
					throw new Unexpected("Impossible create collection " + collectionName, e);
				}
			else
				throw new NotFound("Collection " + collectionName);
		}

		return this.collections.get(collectionName);
	}

	public static String directoryNameOf(String collectionName) {
		CRC32 crc32 = new CRC32();
		crc32.update(collectionName.getBytes());
		return Long.toString(crc32.getValue(), 32);
	}

	private File directoryOf(String collectionName) {
		File collectionDir = new File(this.baseDir, Store.directoryNameOf(collectionName));
		collectionDir.mkdirs();
		return collectionDir;
	}

}
