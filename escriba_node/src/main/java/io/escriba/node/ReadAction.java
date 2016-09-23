package io.escriba.node;

public abstract class ReadAction<T> {

	public static <T> Continue<T> read(int bytes) {
		return new Continue(bytes);
	}

	public static <T> Stop<T> stop(T value) {
		return new Stop<>(value);
	}

	public static final class Continue<T> extends ReadAction<T> {

		public final int bytes;

		public Continue(int bytes) {
			this.bytes = bytes;
		}
	}

	public static class Stop<T> extends ReadAction<T> {
		public final T value;

		public Stop(T value) {
			this.value = value;
		}
	}
}
