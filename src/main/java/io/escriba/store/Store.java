package io.escriba.store;

import io.escriba.Func;

import java.nio.ByteBuffer;

public abstract class Store {

	public abstract StoreCollection collection(String name);

	public interface Fail {
		void apply(Throwable throwable) throws Exception;
	}

	public interface Putter {
		void apply(Integer writen, Func.C2<ByteBuffer, Long> write, Func.C0 close) throws Exception;
	}
}