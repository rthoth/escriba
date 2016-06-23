package io.escriba.store.file;

import io.escriba.functional.Callback2;

import java.nio.ByteBuffer;

class Reader implements Callback2<ByteBuffer, Long> {
	protected final GetContext context;

	public Reader(GetContext context) {
		this.context = context;
	}

	@Override
	public void apply(ByteBuffer buffer, Long position) throws Exception {
		context.channel.read(buffer, position, null, new ReaderHandler(context, buffer));
	}
}
