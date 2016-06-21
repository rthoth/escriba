package io.escriba;

import io.escriba.store.Store;

public class Config {
	public final int dispatcherPool;
	public final int clientPool;
	public final Store mapdb;

	public Config(final int dispatcherPool, final int clientPool, Store mapdb) {
		this.dispatcherPool = dispatcherPool;
		this.clientPool = clientPool;
		this.mapdb = mapdb;
	}
}
