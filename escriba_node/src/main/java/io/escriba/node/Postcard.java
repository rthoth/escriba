package io.escriba.node;

import io.escriba.server.Server;

import java.io.Serializable;
import java.util.concurrent.Future;

public class Postcard implements Serializable {

	public final Anchor anchor;
	public final String collection;

	public Postcard(String collection, Server server) {
		this(collection, new Anchor(server));
	}

	public Postcard(String collection, Anchor anchor) {
		this.collection = collection;
		this.anchor = anchor;
	}

	protected <T> Future<T> get(Node node, String key, int initialSize, PostcardReader<T> reader) throws Exception {
		if (anchor.equals(node.anchor))
			return new LocalGet(node.store, this, key, initialSize, reader).future();
		else
			return new RemoteGet<>(node.bootstrap().clone(), this, key, initialSize, reader).future();
	}

//	public <T> Future<T> get(String key, int initialSize, PostcardReader<T> reader) throws Exception {
//		if (isLocal())
//			return new RemoteGet(this, key, initialSize, reader).future();
//		else
//			return new LocalGet(this, key, initialSize, reader).future();
//	}

//	public <T> Future<Postcard> put(String key, String mediaType, T content, PostcardWriter<T> writer) throws Exception {
//		if (server == null)
//			return new RemotePut<>(this, key, mediaType, content, writer).future();
//		else
//			return new LocalPut<>(this, key, mediaType, content, writer).future();
//	}

	protected <T, P> Future<Postcard> put(Node node, String key, String mediaType, P previous, T content, PostcardWriter<T, P> writer) throws Exception {

		if (anchor.equals(node.anchor))
			return new LocalPut<>(node.store, this, key, mediaType, previous, content, writer).future();
		else
			return new RemotePut<>(node.bootstrap(), this, key, mediaType, previous, content, writer).future();
	}
}
