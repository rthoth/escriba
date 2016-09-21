package io.escriba.node;

import io.escriba.Collection;
import io.escriba.ProxyFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class LocalPut<T> extends Put {

	private final Collection collection;
	private final CompletableFuture<Postcard> completable;
	private final T content;
	private final String key;
	private final String mediaType;
	private final Postcard postcard;
	private final PostcardWriter writer;

	public LocalPut(Postcard postcard, String key, String mediaType, T content, PostcardWriter<T> writer) throws Exception {
		this.postcard = postcard;
		this.key = key;
		this.mediaType = mediaType;
		this.content = content;
		this.writer = writer;
		completable = new CompletableFuture<>();

		collection = postcard.store().collection(postcard.collection(), true);

		collection.put(key, mediaType).ready(control -> {

			ByteBuffer buffer = writer.apply(content);
			if (buffer != null)
				control.write(buffer);
			else
				control.close();

		}).written((total, buffer, control) -> {
			if (buffer.hasRemaining())
				control.write(buffer);
			else {
				buffer = writer.apply(content);

				if (buffer != null)
					control.write(buffer);
				else
					control.close();
			}
		})
			.error(error -> completable.completeExceptionally(error))
			.success(() -> completable.complete(postcard)).start();
	}

	@Override
	public Future<Postcard> future() {
		return new ProxyFuture<>(completable);
	}
}
