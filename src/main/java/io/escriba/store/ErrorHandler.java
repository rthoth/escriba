package io.escriba.store;

public interface ErrorHandler {
	void error(Throwable throwable) throws Exception;
}
