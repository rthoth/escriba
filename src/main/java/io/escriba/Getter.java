package io.escriba;

import java.nio.ByteBuffer;

public interface Getter {

	Getter error(ErrorHandler errorHandler);

	Getter read(ReadHandler readHandler);

	Getter ready(ReadyHandler readyHandler);

	void start();

	interface ReadHandler {
		void apply(int bytes, ByteBuffer buffer, Read read, Close close) throws Exception;
	}

	interface ReadyHandler {
		void apply(DataEntry entry, Read read, Close close) throws Exception;
	}
}
