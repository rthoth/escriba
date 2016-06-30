package io.escriba;

import io.escriba.EscribaException.NotFound;
import io.escriba.EscribaException.Unexpected;
import io.escriba.hash.HashCollection;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

public class Store {

	private final File baseDir;
	private final WeakHashMap<String, HashCollection> collections = new WeakHashMap<>();
	private final DB db;
	final ExecutorService executorService;

	public Store(File mapDBFile, File baseDir, int threads) {
		this.db = DBMaker.fileDB(mapDBFile)
			.transactionEnable()
			.make();

		this.baseDir = baseDir;

		if (threads > 0)
			executorService = Executors.newWorkStealingPool(threads);
		else
			executorService = null;
	}

	public Store(File mapDBFile, File baseDir) {
		this(mapDBFile, baseDir, 0);
	}

	public Collection collection(String collectionName, boolean create) throws Exception {
		if (this.collections.containsKey(collectionName))
			return this.collections.get(collectionName);

		synchronized (this) {
			if (this.collections.containsKey(collectionName))
				return this.collections.get(collectionName);

			if (create || this.db.exists(collectionName))
				try {
					this.collections.put(collectionName, new HashCollection(collectionName, this.db, this.directoryOf(collectionName), executorService));
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
