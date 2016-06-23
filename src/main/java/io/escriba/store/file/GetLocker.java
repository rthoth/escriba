package io.escriba.store.file;

import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;

class GetLocker<A> implements CompletionHandler<FileLock, Object> {
	private final Context<A> context;

	public GetLocker(Context<A> context) {
		this.context = context;
	}

	@Override
	public void completed(FileLock fileLock, Object attachment) {
		context.lock = fileLock;
		context.callUserCode(null);
	}

	@Override
	public void failed(Throwable throwable, Object attachment) {
		context.callFail(throwable);
	}
}
