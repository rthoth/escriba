package io.escriba.hash;

import io.escriba.*;
import io.escriba.functional.T2;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

public class AsyncGet implements Get, Async, Close {

	private static final CompletionHandler<Integer, T2<AsyncGet, ByteBuffer>> READ_HANDLER = new CompletionHandler<Integer, T2<AsyncGet, ByteBuffer>>() {
		@Override
		public void completed(Integer total, T2<AsyncGet, ByteBuffer> t2) {
			t2.a.read(total, t2.b);
		}

		@Override
		public void failed(Throwable throwable, T2<AsyncGet, ByteBuffer> t2) {
			t2.a.error(throwable);
		}
	};

	private AsynchronousFileChannel channel = null;
	private final HashCollection collection;
	private final ErrorHandler errorHandler;
	private final String key;
	private FileLock lock = null;
	private final ReadHandler readHandler;
	private Read reader = null;
	private final ReadyHandler readyHandler;

	public AsyncGet(HashCollection collection, String key, ReadyHandler readyHandler, ReadHandler readHandler, ErrorHandler errorHandler) throws Exception {
		this.collection = collection;
		this.key = key;
		this.readyHandler = readyHandler;
		this.readHandler = readHandler;
		this.errorHandler = errorHandler;
		getChannel();
		locked(null);
	}

	@Override
	public void apply() throws Exception {
		close(true);
	}

	@Override
	public AsynchronousFileChannel getChannel() throws Exception {
		if (channel == null)
			try {
				channel = AsynchronousFileChannel.open(collection.getFile(key).toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ);
			} catch (NoSuchFileException noFileEx) {
				throw new EscribaException.NoValue(key, collection.name, noFileEx);
			}
		return channel;
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	@Override
	public FileLock getLock() {
		return lock;
	}

	@Override
	public void locked(FileLock lock) {
		reader = (buffer, position) -> {
			try {
				channel.read(buffer, position, T2.of(this, buffer), READ_HANDLER);
			} catch (Exception e) {
				error(e);
			}
		};

		try {
			readyHandler.apply(channel.size(), reader, this);
		} catch (Exception e) {
			error(e);
		}
	}

	private void read(Integer total, ByteBuffer buffer) {
		try {
			readHandler.apply(total, buffer, reader, this);
		} catch (Exception e) {
			error(e);
		}
	}
}
