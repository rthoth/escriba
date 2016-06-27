package io.escriba;

import java.nio.ByteBuffer;

public interface Get {
	interface ReadHandler {
		void apply(int total, ByteBuffer buffer, Read read, Close close) throws Exception;
	}

	interface ReadyHandler {
		void apply(Read read, Close close);
	}
}
