package io.escriba;

import io.escriba.hash.HashCollection;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.zip.CRC32;

/**
 * The main class.
 * <p>
 * Everything starts here!
 */
public class Store {

	private static final ForkJoinPool.ForkJoinWorkerThreadFactory THREAD_FACTORY = pool -> {
		ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
		thread.setName("Escriba-WorkerThread-" + pool.getPoolSize());
		return thread;
	};
	private final File baseDir;
	private final WeakHashMap<String, HashCollection> collections = new WeakHashMap<>();
	private final DB db;
	final ExecutorService executorService;

	public Store(File mapDBFile, File baseDir, int threads) {
		this(mapDBFile, baseDir, newExecutorService(threads));
	}

	public Store(File mapDBFile, File baseDir, ExecutorService executorService) {
		try {
			db = DBMaker.fileDB(mapDBFile)
				.transactionEnable()
				.make();
		} catch (Exception e) {
			throw new EscribaException.Unexpected("It's impossible create MapDB control at " + mapDBFile.getAbsolutePath(), e);
		}

		if (baseDir == null)
			throw new EscribaException.IllegalArgument("baseDir is null");

		if (executorService == null)
			throw new EscribaException.IllegalArgument("ExecutorService is null");

		this.baseDir = baseDir;
		this.executorService = executorService;
	}

	public Collection collection(String collectionName, boolean create) throws Exception {
		if (collections.containsKey(collectionName))
			return collections.get(collectionName);

		synchronized (this) {
			if (collections.containsKey(collectionName))
				return collections.get(collectionName);

			if (create || db.exists(collectionName))
				try {
					collections.put(collectionName, new HashCollection(collectionName, db, directoryOf(collectionName), executorService));
				} catch (Exception e) {
					throw new EscribaException.Unexpected("Impossible create collection " + collectionName, e);
				}
			else
				throw new EscribaException.NotFound("Collection " + collectionName);
		}

		return collections.get(collectionName);
	}

	public static String directoryNameOf(String collectionName) {
		CRC32 crc32 = new CRC32();
		crc32.update(collectionName.getBytes());
		return Long.toString(crc32.getValue(), 32);
	}

	private File directoryOf(String collectionName) {
		File collectionDir = new File(baseDir, directoryNameOf(collectionName));
		collectionDir.mkdirs();
		return collectionDir;
	}

	private static ExecutorService newExecutorService(int threads) {
		if (threads > 0)
			return new ForkJoinPool(threads, THREAD_FACTORY, null, true);
		else
			throw new EscribaException.IllegalArgument("threads must be greater than zero!");
	}

}
