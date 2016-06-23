package io.escriba.store.file;

import io.escriba.Func;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

class ReaderHandler implements CompletionHandler<Integer, Object> {

	private final ByteBuffer buffer;
	private final GetContext context;

	public ReaderHandler(GetContext context, ByteBuffer buffer) {
		this.context = context;
		this.buffer = buffer;
	}

	@Override
	public void completed(Integer read, Object attachment) {
		context.callUserCode(Func.t2(read, buffer));
	}

	@Override
	public void failed(Throwable throwable, Object attachment) {
		context.callFail(throwable);
	}
}
