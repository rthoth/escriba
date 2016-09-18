package io.escriba.hash;

import io.escriba.*;
import io.escriba.DataEntry.Status;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Get implements Getter.Control {
	private static final Set<StandardOpenOption> OPEN_OPTIONS = new HashSet<>(Arrays.asList(StandardOpenOption.READ));

	private static final CompletionHandler<Integer, T3<Get, ByteBuffer, Getter.ReadHandler>> READ_HANDLER_NO_UPDATE_POSITION = new CompletionHandler<Integer, T3<Get, ByteBuffer, Getter.ReadHandler>>() {
		@Override
		public void completed(Integer read, T3<Get, ByteBuffer, Getter.ReadHandler> t3) {
			try {
				if (t3.c == null)
					t3.a.readHandler.apply(read, t3.b, t3.a);
				else
					t3.c.apply(read, t3.b, t3.a);
			} catch (Exception e) {
				t3.a.error(e);
			}
		}

		@Override
		public void failed(Throwable throwable, T3<Get, ByteBuffer, Getter.ReadHandler> t3) {
			try {
				t3.a.error(throwable);
			} catch (Throwable e) {
				// TODO: Log?
			}
		}
	};

	private static final CompletionHandler<Integer, T3<Get, ByteBuffer, Getter.ReadHandler>> READ_HANDLER_UPDATE_POSITION = new CompletionHandler<Integer, T3<Get, ByteBuffer, Getter.ReadHandler>>() {
		@Override
		public void completed(Integer read, T3<Get, ByteBuffer, Getter.ReadHandler> t3) {
			t3.a.position += read;
			READ_HANDLER_NO_UPDATE_POSITION.completed(read, t3);
		}

		@Override
		public void failed(Throwable throwable, T3<Get, ByteBuffer, Getter.ReadHandler> t3) {
			READ_HANDLER_NO_UPDATE_POSITION.failed(throwable, t3);
		}
	};

	private AsynchronousFileChannel channel;
	private final HashCollection collection;
	private final CompletableFuture<DataEntry> completable;
	private DataEntry entry;
	private final ErrorHandler errorHandler;
	private Future<DataEntry> future;
	private final String key;
	private long position;
	private final Getter.ReadHandler readHandler;
	private final Getter.ReadyHandler readyHandler;
	private final SuccessHandler successHandler;

	public Get(HashCollection collection, String key, Getter.ReadyHandler readyHandler, Getter.ReadHandler readHandler, ErrorHandler errorHandler, SuccessHandler successHandler) {
		this.collection = collection;
		this.key = key;
		this.readyHandler = readyHandler;
		this.readHandler = readHandler;
		this.errorHandler = errorHandler;
		this.successHandler = successHandler;

		entry = collection.getEntry(key);
		future = new ProxyFuture<>(completable = new CompletableFuture<>());

		if (entry == null)
			throw new EscribaException.NotFound(key + " in collection " + collection.name);

		if (entry.status != Status.Ok)
			throw new EscribaException.IllegalState(key + "in collection " + collection.name + " has status equals to " + entry.status.name());

		if (readHandler != null)
			collection.executor.submit(this::openFile);
		else
			throw new EscribaException.IllegalArgument("The ReadHandler must be defined!");
	}

	@Override
	public void close() {
		if (isOpen()) {
			try {

				entry = entry.copy()
					.access(new Date())
					.end();

				collection.updateEntry(key, entry);

				close0();
			} catch (Throwable throwable) {
				error(throwable);
				return;
			}

			try {
				successHandler.apply();
			} catch (Throwable throwable) {
				// TODO: Log?
			}

			completable.complete(entry);
		}
	}

	private void close0() throws IOException {
		if (isOpen()) {
			channel.close();
			channel = null;
		}
	}

	private void closeQuietly() {
		try {
			close0();
		} catch (Exception e) {
			// TODO: Log?
		}
	}

	private void error(Throwable throwable) {
		error(throwable, false);
	}

	private void error(Throwable throwable, boolean invoke) {
		if (invoke || isOpen()) {

			closeQuietly();

			if (errorHandler != null)
				try {
					errorHandler.apply(throwable);
				} catch (Exception e) {
					// TODO: Log?
				}

			completable.completeExceptionally(throwable);
		}
	}

	public Future<DataEntry> future() {
		return future;
	}

	private boolean isOpen() {
		return channel != null && channel.isOpen();
	}

	private void openFile() {
		try {
			channel = AsynchronousFileChannel.open(collection.getPath(key), OPEN_OPTIONS, collection.executor);
		} catch (Exception e) {
			error(e);
			return;
		}

		try {
			readyHandler.apply(entry, this);
		} catch (Exception e) {
			error(e);
		}
	}

	@Override
	public void read(ByteBuffer buffer) {
		if (isOpen())
			channel.read(buffer, position, T3.of(this, buffer, null), READ_HANDLER_UPDATE_POSITION);
	}

	@Override
	public void read(ByteBuffer buffer, Getter.ReadHandler readHandler) {
		if (isOpen())
			channel.read(buffer, position, T3.of(this, buffer, readHandler), READ_HANDLER_UPDATE_POSITION);
	}

	@Override
	public void read(ByteBuffer buffer, long position) {
		if (isOpen())
			channel.read(buffer, position, T3.of(this, buffer, null), READ_HANDLER_NO_UPDATE_POSITION);
	}

	@Override
	public void read(ByteBuffer buffer, long position, Getter.ReadHandler readHandler) {
		if (isOpen())
			channel.read(buffer, position, T3.of(this, buffer, readHandler), READ_HANDLER_NO_UPDATE_POSITION);
	}
}
