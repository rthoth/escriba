package io.escriba;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * It prepares a put command
 */
public interface Putter {

	/**
	 * Set errorHandler
	 *
	 * @param errorHandler
	 * @return
	 */
	Putter error(ErrorHandler errorHandler);

	/**
	 * Set readyHandler. This handler is invoked when a put command is ready to start!
	 *
	 * @param readyHandler
	 * @return
	 */
	Putter ready(ReadyHandler readyHandler);

	/**
	 * Start put process and returns future when done!
	 *
	 * @return
	 */
	Future<DataEntry> start();

	/**
	 * Set successHandler. This handler is invoked when close is invoked and when no failures!
	 *
	 * @param successHandler
	 * @return
	 */
	Putter success(SuccessHandler successHandler);

	/**
	 * Set WrittenHandler. This handler is invoked when a write command hasn't a handler!
	 *
	 * @param writtenHandler
	 * @return
	 */
	Putter written(WrittenHandler writtenHandler);

	/**
	 * It is used to execute commands in actual put command!
	 */
	interface Control {
		void close();

		@SuppressWarnings("unused")
		void write(ByteBuffer buffer, long position);

		@SuppressWarnings("unused")
		void write(ByteBuffer buffer, WrittenHandler handler);

		@SuppressWarnings("unused")
		void write(ByteBuffer buffer, long position, WrittenHandler handler);

		void write(ByteBuffer buffer);
	}

	interface ReadyHandler {
		void apply(Control control) throws Exception;
	}

	interface WrittenHandler {
		void apply(int written, ByteBuffer buffer, Control control) throws Exception;
	}
}
