package io.escriba.server;

public class Config {
	public static final int DEFAULT_GET_CHUNK_SIZE = 1024 * 512;
	public final int dispatchers;
	public final int getChunkSize;
	public final int workers;

	public Config(int dispatchers, int workers, int chunkSize) {
		this.dispatchers = dispatchers;
		this.workers = workers;
		this.getChunkSize = chunkSize;
	}

	public Config(int dispatchers, int workers) {
		this(dispatchers, workers, DEFAULT_GET_CHUNK_SIZE);
	}
}
