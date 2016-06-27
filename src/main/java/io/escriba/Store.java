package io.escriba;

import io.escriba.hash.HashCollection;
import org.mapdb.DB;
import org.mapdb.DB.HashMapMaker;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;

public class Store {

	private final File dataDir;
	private final DB db;

	public Store(File mapDBFile, File dataDir) {
		db = DBMaker.fileDB(mapDBFile)
			.transactionEnable()
			.make();

		this.dataDir = dataDir;
	}

	public Store(File dataDir) {
		db = DBMaker.memoryDirectDB().transactionEnable().make();
		this.dataDir = dataDir;
	}

	public Collection collection(String collectionName) {
		return collection(collectionName, false);
	}

	public Collection collection(String collectionName, boolean create) {

		HashMapMaker<String, Data> maker = db.hashMap(collectionName, Serializer.STRING, Data.SERIALIZER);

		HTreeMap<String, Data> map = null;

		if (!db.exists(collectionName)) {
			if (create) {
				map = maker.createOrOpen();
			}
		} else
			map = maker.open();

		if (map == null)
			throw new IllegalStateException(String.format("No DB Collection %s", collectionName));

		return new HashCollection(collectionName, map, new File(dataDir, collectionName));
	}
}
