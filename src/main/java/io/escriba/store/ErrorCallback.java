package io.escriba.store;

import io.escriba.functional.Callback1;

public interface ErrorCallback extends Callback1<Throwable> {
	@Override
	void apply(Throwable throwable) throws Exception;
}
