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
	public Store(File controlFile, DataDir[] dataDirs, int threads) {
		this(controlFile, dataDirs, newExecutorService(threads));
	}

	/**
	 * @param controlFile     MapDB File used to control collections and values.
	 * @param dataDirs        Array of directories where escriba will write values.
	 * @param executorService The main execute service
	 */
	public Store(File controlFile, DataDir[] dataDirs, ExecutorService executorService) {
		if (dataDirs == null)
			throw new EscribaException.IllegalArgument("dataDirs is null");

		if (dataDirs.length == 0)
			throw new EscribaException.IllegalArgument("dataDirs don't have elements");

		if (executorService == null)
			throw new EscribaException.IllegalArgument("ExecutorService is null");

		try {
			db = DBMaker.fileDB(controlFile).transactionEnable().make();
		} catch (Exception e) {
			throw new EscribaException.Unexpected("It's impossible create MapDB control at " + controlFile.getAbsolutePath(), e);
		}

		dataDirPool = new DataDirPool.RoundRobin(dataDirs);
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
