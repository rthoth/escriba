package io.escriba.hash;

import io.escriba.Close;
import io.escriba.DataEntry;
import io.escriba.DataEntry.Status;
import io.escriba.ErrorHandler;
import io.escriba.EscribaException.Unexpected;
import io.escriba.Putter.ReadyHandler;
import io.escriba.Putter.WrittenHandler;
import io.escriba.Write;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class AsyncPut implements Close {

	private static final CompletionHandler<Integer, AsyncPut> WRITE_HANDLER = new CompletionHandler<Integer, AsyncPut>() {
		@Override
		public void completed(Integer written, AsyncPut asyncPut) {
			asyncPut.written(written);
		}

		@Override
		public void failed(Throwable exc, AsyncPut attachment) {

		}
	};

	private static final CompletionHandler<FileLock, AsyncPut> LOCK_HANDLER = new CompletionHandler<FileLock, AsyncPut>() {
		@Override
		public void completed(FileLock lock, AsyncPut asyncPut) {
			asyncPut.locked(lock);
		}

		@Override
		public void failed(Throwable throwable, AsyncPut asyncPut) {
			asyncPut.error(throwable);
		}
	};

	private static Set<OpenOption> OPEN_OPTIONS;

	static {
		Set<OpenOption> options = new HashSet<>();
		options.add(StandardOpenOption.CREATE);
		options.add(StandardOpenOption.WRITE);
		OPEN_OPTIONS = Collections.unmodifiableSet(options);
	}

	private final AsynchronousFileChannel channel;
	private final HashCollection collection;
	private DataEntry entry;
	private final ErrorHandler errorHandler;
	private final String key;
	private FileLock lock = null;
	private final ReadyHandler readyHandler;
	private long total = 0;
	private Write write;
	private final WrittenHandler writtenHandler;

	public AsyncPut(HashCollection collection, String key, String mediaType, ReadyHandler readyHandler, WrittenHandler writtenHandler, ErrorHandler errorHandler) {
		this.collection = collection;
		this.key = key;
		this.readyHandler = readyHandler;
		this.writtenHandler = writtenHandler;
		this.errorHandler = errorHandler;

		entry = collection.getOrCreateEntry(key);

		if (entry.path == null)
			entry = entry.path(collection.nextPath());

		if (entry.status != Status.Creating)
			entry = entry.status(Status.Updating);

		collection.update(key, entry.mediaType(mediaType));

		Path path = collection.getPath(entry);
		File parent = path.getParent().toFile();
		if (!parent.exists())
			try {
				parent.mkdirs();
			} catch (Exception e) {
				throw new Unexpected(e);
			}
		try {
			ExecutorService executorService = collection.executorService;

			if (executorService == null)
				channel = AsynchronousFileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			else
				channel = AsynchronousFileChannel.open(path, OPEN_OPTIONS, executorService);

		} catch (IOException e) {
			throw new Unexpected("Impossible open channel to put " + key + " in collection " + collection.name, e);
		}

		channel.lock(this, LOCK_HANDLER);
	}

	@Override
	public void apply() throws Exception {
		entry = entry.status(Status.Ok);
		close(true);
	}

	private void close(boolean callErrorHandler) {
		if (channel.isOpen()) {

			if (lock != null)
				try {
					lock.close();
					lock = null;
				} catch (Exception e) {
					if (callErrorHandler && errorHandler != null)
						try {
							errorHandler.apply(e);
						} catch (Exception e1) {
							// TODO: Log?
						}
				}

			try {
				collection.update(key, entry.size(channel.size()));
			} catch (Exception e) {
				if (callErrorHandler && errorHandler != null)
					try {
						errorHandler.apply(e);
					} catch (Exception e1) {
						// TODO: Log?
					}
			}

			try {
				channel.close();
			} catch (Exception e) {
				if (callErrorHandler && errorHandler != null)
					try {
						errorHandler.apply(e);
					} catch (Exception e1) {
						// TODO: Log?
					}
			}
		}
	}

	private void error(Throwable throwable) {
		close(false);

		if (errorHandler != null)
			try {
				errorHandler.apply(throwable);
			} catch (Exception e) {
				// TODO: Log?
			}

	}

	private void locked(FileLock lock) {
		this.lock = lock;

		write = (buffer, position) -> {
			try {
				channel.write(buffer, position, this, WRITE_HANDLER);
			} catch (Exception e) {
				error(e);
			}
		};

		if (readyHandler != null)
			try {
				readyHandler.apply(write, this);
			} catch (Exception e) {
				error(e);
			}
		else if (writtenHandler != null)
			written(0);
		else
			close(false);
	}

	private void written(int written) {
		total += written;

		try {
			writtenHandler.apply(total, written, write, this);
		} catch (Exception e) {
			error(e);
		}
	}
}
