package io.escriba.node;

import io.escriba.server.Server;

import java.util.concurrent.Future;

public class Postcard {
	private final Anchor anchor;
	private final String collection;
	private final transient Server server;

	public Postcard(String collection, Server server) {
		this.anchor = new Anchor(server);
		this.collection = collection;
		this.server = server;
	}

	public <T> Future<Postcard> put(String key, String mediaType, T content, PostcardWriter<T> writer) {
		if (server == null)
			return new RemotePut<>(this, key, mediaType, content, writer).start();
		else
			return new LocalPut<>(this, key, mediaType, content, writer).start();
	}

}
