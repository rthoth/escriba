package io.escriba.store.file;

import io.escriba.functional.Callback2;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

class Writer implements Callback2<ByteBuffer, Long>, CompletionHandler<Integer, Object> {

	private final PutContext context;

	public Writer(PutContext context) {
		this.context = context;
	}

	@Override
	public void apply(ByteBuffer buffer, Long position) throws Exception {
		context.channel.write(buffer, position, null, this);
	}

	@Override
	public void completed(Integer writen, Object attachment) {
		context.callUserCode(writen);
	}

	@Override
	public void failed(Throwable throwable, Object attachment) {
		context.callFail(throwable);
	}
}
