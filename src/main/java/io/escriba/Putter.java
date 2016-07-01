package io.escriba;

public interface Putter {

	Putter error(ErrorHandler errorHandler);

	Putter ready(ReadyHandler readyHandler);

	void start();

	Putter written(WrittenHandler writtenHandler);

	interface ReadyHandler {
		void apply(Write write, Close close) throws Exception;
	}

	interface WrittenHandler {
		void apply(long total, int last, Write write, Close close) throws Exception;
	}
}
