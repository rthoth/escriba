package io.escriba.server;

public class Config {
	public static final int DEFAULT_GET_CHUNK_SIZE = 1024 * 512;

	public static final int DEFAULT_PUT_CACHE_SIZE = 1024 * 256;

	private static final int DEFAULT_PUT_MAX_FRAMES = 1024 * 32;

	public final int dispatchers;

	public final int getChunkSize;

	public final int putCacheSize;

	public final int putMaxFrames;

	public final int workers;

	public Config(int dispatchers, int workers, int getChunkSize, int putCacheSize, int putMaxFrames) {
		this.dispatchers = dispatchers;
		this.workers = workers;
		this.getChunkSize = getChunkSize;
		this.putCacheSize = putCacheSize;
		this.putMaxFrames = putMaxFrames;
	}

	public Config(int dispatchers, int workers) {
		this(dispatchers, workers, DEFAULT_GET_CHUNK_SIZE, DEFAULT_PUT_CACHE_SIZE, DEFAULT_PUT_MAX_FRAMES);
	}
}
