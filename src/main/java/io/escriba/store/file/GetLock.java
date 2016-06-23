package io.escriba.store.file;

import io.escriba.store.ErrorHandler;

import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;

public abstract class GetLock {


	public static final CompletionHandler<FileLock, io.escriba.store.file.AsyncOperation<? extends FileValue, ? extends ErrorHandler>> AsyncOperation = new CompletionHandler<FileLock, io.escriba.store.file.AsyncOperation<? extends FileValue, ? extends ErrorHandler>>() {
		@Override
		public void completed(FileLock result, AsyncOperation<? extends FileValue, ? extends ErrorHandler> attachment) {
			attachment.locked(result);
		}

		@Override
		public void failed(Throwable throwable, AsyncOperation<? extends FileValue, ? extends ErrorHandler> attachment) {
			attachment.failed(throwable);
		}
	};
}
