package io.escriba.node;

import io.escriba.Store;
import io.escriba.server.Server;

import java.util.concurrent.Future;

public class Postcard {
	private final Anchor anchor;
	private final String collection;
	private final transient Server server;
	private final transient Store store;

	public Postcard(String collection, Server server) {
		this.anchor = new Anchor(server);
		this.collection = collection;
		this.server = server;
		this.store = server.store();
	}

	public String collection() {
		return collection;
	}

	public <T> Future<T> get(String key, int initialSize, PostcardReader<T> reader) throws Exception {
		if (server == null)
			return new RemoteGet(this, key, initialSize, reader).future();
		else
			return new LocalGet(this, key, initialSize, reader).future();
	}

	public <T> Future<Postcard> put(String key, String mediaType, T content, PostcardWriter<T> writer) throws Exception {
		if (server == null)
			return new RemotePut<>(this, key, mediaType, content, writer).future();
		else
			return new LocalPut<>(this, key, mediaType, content, writer).future();
	}

	public Store store() {
		return store;
	}
}
