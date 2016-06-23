package io.escriba.store;

import io.escriba.functional.Callback2;
import io.escriba.functional.Callback3;

import java.nio.ByteBuffer;

public abstract class Put {

	protected ErrorCallback errorCallback = null;
	protected ReadyCallback readyCallback = null;
	protected WriteCallback writeCallback = null;

	public Put onError(ErrorCallback error) {
		this.errorCallback = error;
		return this;
	}

	public Put onReady(ReadyCallback ready) {
		this.readyCallback = ready;
		return this;
	}

	public Put onWrite(WriteCallback write) {
		this.writeCallback = write;
		return this;
	}

	public abstract void start();

	public interface ReadyCallback extends Callback2<Write, Close> {
		@Override
		void apply(Write write, Close close) throws Exception;
	}

	public interface Write extends Callback2<ByteBuffer, Long> {
		@Override
		void apply(ByteBuffer buffer, Long position) throws Exception;
	}

	public interface WriteCallback extends Callback3<Integer, Write, Close> {
		@Override
		void apply(Integer writen, Write write, Close close) throws Exception;
	}
}
