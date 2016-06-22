package io.escriba.store;

import io.escriba.functional.*;

import java.nio.ByteBuffer;

public abstract class Store {

	public abstract StoreCollection collection(String name);

	public interface Fail extends Callback1<Throwable> {

	}

	public interface Getter extends Callback3<T2<Integer, ByteBuffer>, Callback2<ByteBuffer, Long>, Callback0> {
		@Override
		void apply(T2<Integer, ByteBuffer> last, Callback2<ByteBuffer, Long> read, Callback0 close) throws Exception;
	}

	public interface Putter extends Callback3<Integer, Callback2<ByteBuffer, Long>, Callback0> {
		@Override
		void apply(Integer read, Callback2<ByteBuffer, Long> write, Callback0 close) throws Exception;
	}
}