package io.escriba;

import io.escriba.hash.HashCollection;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.nio.file.Path;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.zip.CRC32;

/**
 * Main class.
 * <p>
 * In eScriba everything is asynchronous.
 */
public class Store {

	/**
	 * Default ForkJoinThread Factory
	 */
	private static final ForkJoinPool.ForkJoinWorkerThreadFactory THREAD_FACTORY = pool -> {
		ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
		thread.setName("eScriba-WorkerThread-" + pool.getPoolSize());
		return thread;
	};

	private final WeakHashMap<String, HashCollection> collections = new WeakHashMap<>();
	private final DataDirPool dataDirPool;
	private final DB db;
	final ExecutorService executorService;

	@SuppressWarnings("unused")
	public Store(File controlFile, T2<Path, Integer>[] dataDirs, int threads) {
		this(controlFile, dataDirs, newExecutorService(threads));
	}

	@SuppressWarnings("unused")
	public Store(File controlFile, Path dataDir, int threads) {
		this(controlFile, dataDir, newExecutorService(threads));
	}

	public Store(File controlFile, Path dataDir, ExecutorService executorService) {
		this(controlFile, new DataDirPool.Fixed(dataDir), executorService);
	}

	public Store(File controlFile, T2<Path, Integer>[] dataDirs, ExecutorService executorService) {
		this(controlFile, new DataDirPool.RoundRobin(dataDirs), executorService);
	}

	public Store(File controlFile, DataDirPool dataDirPool, ExecutorService executorService) {
		if (executorService == null)
			throw new EscribaException.IllegalArgument("ExecutorService is null");

		try {
			db = DBMaker.fileDB(controlFile).transactionEnable().make();
		} catch (Exception e) {
			throw new EscribaException.Unexpected("It's impossible create MapDB control at " + controlFile.getAbsolutePath(), e);
		}

		this.dataDirPool = dataDirPool.copy();
		this.executorService = executorService;
	}

	public Collection collection(String collectionName, boolean create) throws Exception {
		if (collections.containsKey(collectionName))
			return collections.get(collectionName);

		synchronized (this) {
			if (collections.containsKey(collectionName))
				return collections.get(collectionName);

			if (create)
				try {
					collections.put(collectionName, new HashCollection(collectionName, db, dataDirPool.copy(), executorService));
				} catch (Exception e) {
					throw new EscribaException.Unexpected("Impossible create collection " + collectionName, e);
				}
			else
				throw new EscribaException.NotFound("Collection " + collectionName);
		}

		return collections.get(collectionName);
	}

	public static String collectionDirName(String collectionName) {
		CRC32 crc32 = new CRC32();
		crc32.update(collectionName.getBytes());
		return Long.toString(crc32.getValue(), 32);
	}

	private static ExecutorService newExecutorService(int threads) {
		if (threads > 0)
			return new ForkJoinPool(threads, THREAD_FACTORY, null, true);
		else
			throw new EscribaException.IllegalArgument("threads must be greater than zero!");
	}

}
