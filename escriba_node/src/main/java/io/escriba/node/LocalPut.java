package io.escriba.node;

import io.escriba.ProxyFuture;
import io.escriba.Store;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

public class LocalPut<T> extends Put<T> {

	public LocalPut(Store store, Postcard postcard, String key, String mediaType, T content, PostcardWriter<T> writer) throws Exception {
		super(postcard, key, mediaType, content, writer);

		store.collection(postcard.collection, true).put(key, mediaType).ready(control -> {

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
