package io.escriba.node;

import io.escriba.ProxyFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class Put<T> {

	protected final CompletableFuture<Postcard> completable;
	protected final T content;
	protected final String key;
	protected final String mediaType;
	protected final Postcard postcard;
	protected final PostcardWriter<T> writer;

	public Put(Postcard postcard, String key, String mediaType, T content, PostcardWriter<T> writer) {
		this.postcard = postcard;
		this.key = key;
		this.mediaType = mediaType;
		this.content = content;
		this.writer = writer;
		this.completable = new CompletableFuture<>();
	}

	@SuppressWarnings("unused")
	public Future<Postcard> future() {
		return new ProxyFuture<>(completable);
	}
}
