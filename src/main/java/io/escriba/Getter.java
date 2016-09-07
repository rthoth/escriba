package io.escriba;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

public interface Getter {

	Getter error(ErrorHandler errorHandler);

	Getter read(ReadHandler readHandler);

	Getter ready(ReadyHandler readyHandler);

	Future<DataEntry> start();

	Getter success(SuccessHandler successHandler);

	interface Control {
		@SuppressWarnings("unused")
		void close();

		@SuppressWarnings("unused")
		void read(ByteBuffer buffer);

		@SuppressWarnings("unused")
		void read(ByteBuffer buffer, ReadHandler readHandler);

		@SuppressWarnings("unused")
		void read(ByteBuffer buffer, long position);

		@SuppressWarnings("unused")
		void read(ByteBuffer buffer, long position, ReadHandler readHandler);
	}

	interface ReadHandler {
		void apply(int bytes, ByteBuffer buffer, Control control) throws Exception;
	}

	interface ReadyHandler {
		void apply(DataEntry entry, Control control) throws Exception;
	}
}
