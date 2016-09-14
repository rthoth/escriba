package io.escriba.hash;

import io.escriba.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Put implements Putter.Control {

	private static final CompletionHandler<FileLock, Put> LOCK_HANDLER = new CompletionHandler<FileLock, Put>() {
		@Override
		public void completed(FileLock lock, Put put) {
			put.locked(lock);
		}

		@Override
		public void failed(Throwable throwable, Put put) {
			put.error(throwable);
			put.closeQuietly();
		}
	};

	private static final Set<OpenOption> OPEN_OPTIONS = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));

	private static final CompletionHandler<Integer, T3<Put, ByteBuffer, Putter.WrittenHandler>> WRITE_HANDLER_NO_UPDATE_POSITION = new CompletionHandler<Integer, T3<Put, ByteBuffer, Putter.WrittenHandler>>() {
		@Override
		public void completed(Integer total, T3<Put, ByteBuffer, Putter.WrittenHandler> t3) {
			Putter.WrittenHandler handler = t3.c == null ? t3.a.writtenHandler : t3.c;

			try {
				handler.apply(total, t3.b, t3.a);
			} catch (Exception e) {
				t3.a.error(e);
			}

		}

		@Override
		public void failed(Throwable throwable, T3<Put, ByteBuffer, Putter.WrittenHandler> t3) {
			t3.a.error(throwable);
		}
	};

	private static final CompletionHandler<Integer, T3<Put, ByteBuffer, Putter.WrittenHandler>> WRITE_HANDLER_UPDATE_POSITION = new CompletionHandler<Integer, T3<Put, ByteBuffer, Putter.WrittenHandler>>() {
		@Override
		public void completed(Integer total, T3<Put, ByteBuffer, Putter.WrittenHandler> t3) {
			t3.a.position += total;

			WRITE_HANDLER_NO_UPDATE_POSITION.completed(total, t3);
		}

		@Override
		public void failed(Throwable throwable, T3<Put, ByteBuffer, Putter.WrittenHandler> t3) {
			t3.a.error(throwable);
		}
	};


	private AsynchronousFileChannel channel;
	private final HashCollection collection;
	private final CompletableFuture<DataEntry> completable;
	private DataEntry entry;
	private final ErrorHandler errorHandler;
	private final ProxyFuture<DataEntry> future;
	private final String key;
	private FileLock lock;
	private final String mediaType;
	private long position;
	private final Putter.ReadyHandler readyHandler;
	private final SuccessHandler successHandler;
	private final Putter.WrittenHandler writtenHandler;

	public Put(HashCollection collection, String key, String mediaType, Putter.ReadyHandler readyHandler, Putter.WrittenHandler writtenHandler, ErrorHandler errorHandler, SuccessHandler successHandler) {
		this.collection = collection;
		this.key = key;
		this.mediaType = mediaType;
		this.readyHandler = readyHandler;
		this.writtenHandler = writtenHandler;
		this.errorHandler = errorHandler;
		this.successHandler = successHandler;

		if (readyHandler == null)
			throw new EscribaException.IllegalArgument("The readyHandler must be defined!");

		if (writtenHandler != null) {
			future = new ProxyFuture(completable = new CompletableFuture<>());
			collection.executor.submit(this::openAndLockFile);
		} else
			throw new EscribaException.IllegalArgument("The writtenHandler must be defined!");
	}

	@Override
	public void close() {
		if (channel != null && channel.isOpen()) {

			try {
				entry = entry.copy()
					.size(channel.size())
					.access(new Date())
					.status(DataEntry.Status.Ok)
					.end();

				collection.updateEntry(key, entry);
			} catch (Exception e) {
				error(e);
				return;
			}

			try {
				close0();
			} catch (Exception e) {
				error(e);
				return;
			}

			try {
				if (successHandler != null)
					successHandler.apply();
			} catch (Exception e) {
				// TODO: Log?
			}

			completable.complete(entry);
		}
	}

	private void close0() throws IOException {
		if (lock != null)
			lock.close();

		lock = null;

		if (channel != null && channel.isOpen())
			channel.close();

		channel = null;
	}

	private void closeQuietly() {
		try {
			close0();
		} catch (IOException e) {
			// TODO: Log?
		}
	}

	private void error(Throwable throwable) {
		error(throwable, false);
	}

	private void error(Throwable throwable, boolean invoke) {
		if (invoke || (channel != null && channel.isOpen())) {
			closeQuietly();

			try {
				if (errorHandler != null)
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

	private void locked(FileLock lock) {
		this.lock = lock;

		try {
			readyHandler.apply(this);
		} catch (Exception e) {
			error(e);
		}
	}

	private void openAndLockFile() {
		try {
			entry = collection.getEntry(key);

			entry = (entry == null) ?
				collection.getOrCreateEntry(key).copy()
					.mediaType(mediaType)
					.status(DataEntry.Status.Creating)
					.end()
				:
				entry.copy()
					.mediaType(mediaType)
					.status(DataEntry.Status.Updating)
					.end();

		} catch (Throwable throwable) {
			error(throwable, true);
			return;
		}

		try {
			Path path = collection.getPath(key);

			File parent = path.getParent().toFile();
			if (!parent.exists())
				parent.mkdirs();

			channel = AsynchronousFileChannel.open(path, OPEN_OPTIONS, collection.executor);
		} catch (Throwable throwable) {
			error(throwable, true);
			return;
		}

		channel.lock(this, LOCK_HANDLER);
	}

	@Override
	public void write(ByteBuffer buffer, long position) {
		channel.write(buffer, position, T3.of(this, buffer, null), WRITE_HANDLER_NO_UPDATE_POSITION);
	}

	@Override
	public void write(ByteBuffer buffer, Putter.WrittenHandler handler) {
		channel.write(buffer, position, T3.of(this, buffer, handler), WRITE_HANDLER_UPDATE_POSITION);
	}

	@Override
	public void write(ByteBuffer buffer, long position, Putter.WrittenHandler handler) {
		channel.write(buffer, position, T3.of(this, buffer, handler), WRITE_HANDLER_NO_UPDATE_POSITION);
	}

	@Override
	public void write(ByteBuffer buffer) {
		channel.write(buffer, position, T3.of(this, buffer, null), WRITE_HANDLER_UPDATE_POSITION);
	}
}