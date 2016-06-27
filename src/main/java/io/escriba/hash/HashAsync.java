package io.escriba.hash;

import io.escriba.Data;
import io.escriba.ErrorHandler;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public abstract class HashAsync<H extends ErrorHandler> {

	protected static final CompletionHandler<FileLock, HashAsync<?>> LOCKED_HANDLER = new CompletionHandler<FileLock, HashAsync<?>>() {
		@Override
		public void completed(FileLock result, HashAsync<?> attachment) {
			attachment.lock = result;
			attachment.locked();
		}

		@Override
		public void failed(Throwable throwable, HashAsync<?> attachment) {
			attachment.error(throwable);
		}
	};
	protected AsynchronousFileChannel channel;
	protected final H handler;
	private FileLock lock = null;

	public HashAsync(H handler) {
		this.handler = handler;
	}

	protected void close(boolean callError) {
		if (lock != null)
			try {
				lock.close();
			} catch (Exception e) {
				// TODO: Log?
			}

		if (channel != null && channel.isOpen())
			try {
				channel.close();
				channel = null;
			} catch (Exception e) {
				// TODO: Log?
				if (callError)
					try {
						handler.error(e);
					} catch (Exception e1) {
						// TODO: Log?
					}
			}
	}

	protected void error(Throwable throwable) {
		close(false);
		try {
			handler.error(throwable);
		} catch (Exception e) {

		}
	}

	protected abstract Path getPath(Data data);

	protected abstract Data loadData();

	protected void lock() throws Exception {
		Data data = loadData();

		switch (data.status) {
			case Creating:
				// TODO: What to do?
				break;
			case Ok:
				channel = AsynchronousFileChannel.open(getPath(data), CREATE, WRITE);
				channel.lock(this, LOCKED_HANDLER);
				break;
			case Updating:
				// TODO: Conflict?
				break;
			case Deleting:
				// TODO: Conflict?
				break;
		}
	}

	protected abstract void locked();
}
