package io.escriba.store;

import io.escriba.functional.Callback2;

import java.nio.ByteBuffer;

public interface Put {

	Put async(PutHandler putHandler) throws StoreException;

	interface PutHandler extends ErrorHandler {
		void data(int written, ByteBuffer buffer, Write write, Close close) throws Exception;

		void ready(Write write, Close close) throws Exception;
	}

	interface Write extends Callback2<ByteBuffer, Long> {
		@Override
		void apply(ByteBuffer buffer, Long position) throws Exception;
	}
}
