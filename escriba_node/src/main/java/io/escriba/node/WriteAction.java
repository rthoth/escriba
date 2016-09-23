package io.escriba.node;

import java.nio.ByteBuffer;

public abstract class WriteAction<T> {

	public final ByteBuffer buffer;

	public WriteAction(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public static <T> Stop<T> stop(ByteBuffer buffer) {
		return new Stop<>(buffer);
	}

	public static <T> Continue<T> write(ByteBuffer buffer, T previous) {
		return new Continue<>(buffer, previous);
	}

	public static class Continue<T> extends WriteAction<T> {

		public final T previous;

		public Continue(ByteBuffer buffer, T previous) {
			super(buffer);
			this.previous = previous;
		}
	}

	public static class Stop<T> extends WriteAction<T> {
		public Stop(ByteBuffer buffer) {
			super(buffer);
		}
	}
}
