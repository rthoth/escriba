package io.escriba.server;

import io.escriba.Collection;
import io.escriba.Store;
import io.netty.handler.codec.http.HttpRequest;

public class Request {
	final String collectionName;
	final Config config;
	final HttpRequest httpRequest;
	final String key;
	final Store store;

	public Request(Config config, Store store, String collectionName, String key, HttpRequest request) {
		this.config = config;
		this.store = store;
		this.collectionName = collectionName;
		this.key = key;
		httpRequest = request;
	}

	public Collection collection() throws Exception {
		return this.store.collection(this.collectionName, true);
	}
}
