package io.escriba.store.file;

import io.escriba.ErrorHandler;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;

public abstract class AsyncOperation<V extends FileValue, H extends ErrorHandler> {

	protected final AsynchronousFileChannel channel;
	protected final H handler;
	private FileLock lock = null;
	protected boolean open = true;
	protected final V value;

	public AsyncOperation(V value, AsynchronousFileChannel channel, H handler) {
		this.value = value;
		this.channel = channel;
		this.handler = handler;
	}

	protected void close(boolean callError) {
		if (open) {
			open = false;

			if (lock != null) {
				try {
					lock.close();
				} catch (Exception e) {

				} finally {
					lock = null;
				}
			}

			if (channel.isOpen()) {
				try {
					channel.close();
				} catch (Exception e) {
					if (callError)
						error(e);
				}
			}
		}
	}

	protected void error(Throwable throwable) {
		try {
			handler.error(throwable);
		} catch (Exception e) {
		}
	}

	protected void failed(Throwable throwable) {
		if (open) {
			close(false);
			error(throwable);
		}
	}

	protected void lock() {
		channel.lock(this, GetLock.AsyncOperation);
	}

	protected void locked(FileLock lock) {
		this.lock = lock;
		locked();
	}

	protected abstract void locked();
}
