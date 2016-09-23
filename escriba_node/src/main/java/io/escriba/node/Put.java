package io.escriba.node;

import io.escriba.ProxyFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class Put<T, P> {

	protected final CompletableFuture<Postcard> completable;
	protected final T content;
	protected final String key;
	protected final String mediaType;
	protected final Postcard postcard;
	protected P previous;
	protected final PostcardWriter<T, P> writer;

	public Put(Postcard postcard, String key, String mediaType, P previous, T content, PostcardWriter<T, P> writer) {
		this.postcard = postcard;
		this.key = key;
		this.mediaType = mediaType;
		this.content = content;
		this.writer = writer;
		this.previous = previous;
		this.completable = new CompletableFuture<>();
	}

	@SuppressWarnings("unused")
	public Future<Postcard> future() {
		return new ProxyFuture<>(completable);
	}
}
