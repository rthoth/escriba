package io.escriba.hash;

import io.escriba.ErrorHandler;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;

public interface Async {

	CompletionHandler<FileLock, Async> LOCK_HANDLER = new CompletionHandler<FileLock, Async>() {
		@Override
		public void completed(FileLock lock, Async async) {
			async.locked(lock);
		}

		@Override
		public void failed(Throwable throwable, Async async) {
			async.error(throwable);
		}
	};

	default void close(boolean callError) {
		try {
			AsynchronousFileChannel channel = getChannel();
			if (channel != null && channel.isOpen()) {
				FileLock lock = getLock();
				if (lock != null)
					lock.close();

				getChannel().close();
			}
		} catch (Exception e) {
			if (callError && getErrorHandler() != null)
				try {
					getErrorHandler().apply(e);
				} catch (Exception e1) {
					// TODO: Log?
				}
		}
	}

	default void error(Throwable throwable) {
		close(false);
		if (isOpen()) {
			ErrorHandler handler = getErrorHandler();
			if (handler != null)
				try {
					handler.apply(throwable);
				} catch (Exception e) {
					// TODO: Log?
				}
		}
	}

	AsynchronousFileChannel getChannel() throws Exception;

	ErrorHandler getErrorHandler();

	FileLock getLock();

	default boolean isOpen() {
		try {
			return getChannel().isOpen();
		} catch (Exception e) {
			// TODO: Log?
			return false;
		}
	}

	default void lock() throws Exception {
		getChannel().lock(this, LOCK_HANDLER);
	}

	void locked(FileLock lock);
}
