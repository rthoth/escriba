package io.escriba.store.file;

import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;

class GetLocker<A> implements CompletionHandler<FileLock, Object> {
	private final Context context;

	public GetLocker(Context context) {
		this.context = context;
	}

	@Override
	public void completed(FileLock fileLock, Object attachment) {
		context.locked(fileLock);
	}

	@Override
	public void failed(Throwable throwable, Object attachment) {
		context.callFail(throwable);
	}
}
