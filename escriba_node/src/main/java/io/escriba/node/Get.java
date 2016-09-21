package io.escriba.node;

import io.escriba.ProxyFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class Get<T> {
	protected final CompletableFuture<T> completable = new CompletableFuture<>();
	protected final int initialSize;
	protected final String key;
	protected final Postcard postcard;
	protected final PostcardReader<T> reader;

	public Get(Postcard postcard, String key, int initialSize, PostcardReader<T> reader) {
		this.postcard = postcard;
		this.initialSize = initialSize;
		this.key = key;
		this.reader = reader;
	}

	public Future<T> future() {
		return new ProxyFuture<>(completable);
	}
}
