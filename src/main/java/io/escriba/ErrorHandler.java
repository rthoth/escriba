package io.escriba;

public interface ErrorHandler {
	void apply(Throwable throwable) throws Exception;
}
