package io.escriba.server;

import io.escriba.Collection;
import io.escriba.Store;
import io.netty.handler.codec.http.FullHttpRequest;

public class EscribaRequest {
	final String collectionName;
	final Config config;
	final FullHttpRequest httpRequest;
	final String key;
	final Store store;

	public EscribaRequest(Config config, Store store, String collectionName, String key, FullHttpRequest request) {
		this.config = config;
		this.store = store;
		this.collectionName = collectionName;
		this.key = key;
		this.httpRequest = request;
	}

	public Collection collection() throws Exception {
		return store.collection(collectionName, true);
	}
}
