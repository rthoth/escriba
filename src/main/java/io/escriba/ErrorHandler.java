package io.escriba;

public interface ErrorHandler {
	void error(Throwable throwable) throws Exception;
}
