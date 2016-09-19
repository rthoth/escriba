package io.escriba.node;

import io.escriba.ProxyFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class LocalPut<T> extends Put {

	private final CompletableFuture<Postcard> completable;
	private final T content;
	private final String key;
	private final String mediaType;
	private final Postcard postcard;
	private final PostcardWriter writer;

	public LocalPut(Postcard postcard, String key, String mediaType, T content, PostcardWriter<T> writer) {
		this.postcard = postcard;
		this.key = key;
		this.mediaType = mediaType;
		this.content = content;
		this.writer = writer;
		completable = new CompletableFuture<>();
	}

	@Override
	public Future<Postcard> start() {
		return new ProxyFuture<>(completable);
	}
}
