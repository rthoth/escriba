package io.escriba;

import java.nio.ByteBuffer;

public interface Put {
	interface ReadyHandler {
		void apply(Write write, Close close);
	}

	interface WrittenHandler {
		void apply(int total, ByteBuffer buffer, Write write, Close close) throws Exception;
	}
}
