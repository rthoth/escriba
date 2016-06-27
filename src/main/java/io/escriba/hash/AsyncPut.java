package io.escriba.hash;

import io.escriba.Close;
import io.escriba.ErrorHandler;
import io.escriba.Put;
import io.escriba.Write;
import io.escriba.functional.T2;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class AsyncPut implements Async, Close, Put {

	private static final CompletionHandler<Integer, T2<AsyncPut, ByteBuffer>> WRITE_HANDLER = new CompletionHandler<Integer, T2<AsyncPut, ByteBuffer>>() {
		@Override
		public void completed(Integer total, T2<AsyncPut, ByteBuffer> t2) {
			t2.a.written(total, t2.b);
		}

		@Override
		public void failed(Throwable exc, T2<AsyncPut, ByteBuffer> attachment) {

		}
	};

	private AsynchronousFileChannel channel = null;
	private final HashCollection collection;
	private final ErrorHandler errorHandler;
	private final String key;
	private FileLock lock = null;
	private final Put.ReadyHandler readyHandler;
	private Write write = null;
	private final Put.WrittenHandler writtenHandler;

	public AsyncPut(HashCollection collection, String key, Put.ReadyHandler readyHandler, Put.WrittenHandler writtenHandler, ErrorHandler errorHandler) throws Exception {
		this.collection = collection;
		this.key = key;
		this.readyHandler = readyHandler;
		this.writtenHandler = writtenHandler;
		this.errorHandler = errorHandler;

		lock();
	}

	@Override
	public void apply() {
		close(true);
	}

	@Override
	public AsynchronousFileChannel getChannel() throws Exception {
		if (channel == null)
			channel = AsynchronousFileChannel.open(collection.getFile(key).toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

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
		this.lock = lock;
		ready();
	}

	private void ready() {
		write = (buffer, position) -> {
			if (isOpen()) {
				try {
					channel.write(buffer, position, T2.of(this, buffer), WRITE_HANDLER);
				} catch (Exception e) {
					error(e);
				}
			}
		};

		try {
			readyHandler.apply(write, this);
		} catch (Exception e) {
			error(e);
		}
	}

	private void written(Integer total, ByteBuffer buffer) {
		try {
			writtenHandler.apply(total, buffer, write, this);
		} catch (Exception e) {
			error(e);
		}
	}
}
