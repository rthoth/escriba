package io.escriba.server;

import io.escriba.Collection;
import io.escriba.Store;
import io.netty.handler.codec.http.HttpRequest;

public class EscribaRequest {
	final String collectionName;
	final Config config;
	final HttpRequest httpRequest;
	final String key;
	final Store store;

	public EscribaRequest(Config config, Store store, String collectionName, String key, HttpRequest request) {
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
