package io.escriba.store;

import io.escriba.Close;
import io.escriba.ErrorHandler;
import io.escriba.functional.Callback2;

import java.nio.ByteBuffer;

public interface Get {

	Get async(GetHandler handler);

	interface GetHandler extends ErrorHandler {
		void data(int total, ByteBuffer buffer, Read read, Close close) throws Exception;

		void ready(Read read, Close close) throws Exception;
	}

	interface Read extends Callback2<ByteBuffer, Long> {
		@Override
		void apply(ByteBuffer buffer, Long position) throws Exception;
	}
}
