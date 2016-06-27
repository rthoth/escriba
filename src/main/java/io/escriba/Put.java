package io.escriba;

import java.nio.ByteBuffer;

public interface Put {

	Put async(PutHandler handler) throws Exception;

	interface PutHandler extends ErrorHandler {
		void ready(Write write, Close close);

		void written(int total, ByteBuffer buffer, Write write, Close close);
	}
}
