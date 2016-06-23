package io.escriba.server;

import io.escriba.store.Store;

public class Config {
	public final int clientPool;
	public final int dispatcherPool;
	public final Store store;

	public Config(final int dispatcherPool, final int clientPool, Store store) {
		this.dispatcherPool = dispatcherPool;
		this.clientPool = clientPool;
		this.store = store;
	}
}
