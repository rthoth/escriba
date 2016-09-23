package io.escriba.node;

import io.escriba.ProxyFuture;
import io.escriba.Store;

import java.util.concurrent.Future;

public class LocalPut<T, P> extends Put<T, P> {

	private boolean shouldClose;

	public LocalPut(Store store, Postcard postcard, String key, String mediaType, P previous, T content, PostcardWriter<T, P> writer) throws Exception {
		super(postcard, key, mediaType, previous, content, writer);

		store.collection(postcard.collection, true).put(key, mediaType).ready(control -> {

//			ByteBuffer buffer = writer.apply(content, previous);
			WriteAction<P> action = writer.apply(content, this.previous);

			if (action instanceof WriteAction.Continue)
				this.previous = ((WriteAction.Continue<P>) action).previous;

			else if (action instanceof WriteAction.Stop)
				this.shouldClose = true;

			control.write(action.buffer);

		}).written((total, buffer, control) -> {
			if (buffer.hasRemaining())
				control.write(buffer);
			else if (!shouldClose) {
				WriteAction action = writer.apply(content, this.previous);

				if (action instanceof WriteAction.Continue)
					this.previous = ((WriteAction.Continue<P>) action).previous;

				else if (action instanceof WriteAction.Stop)
					this.shouldClose = true;

				control.write(action.buffer);
			} else
				control.close();
		})
			.error(error -> completable.completeExceptionally(error))
			.success(() -> completable.complete(postcard)).start();
	}

	@Override
	public Future<Postcard> future() {
		return new ProxyFuture<>(completable);
	}
}
