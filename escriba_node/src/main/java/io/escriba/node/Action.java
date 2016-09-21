package io.escriba.node;

public abstract class Action<T> {

	public static <T> Read<T> read(int bytes) {
		return new Read<>(bytes);
	}

	public static <T> Stop<T> stop(T value) {
		return new Stop<>(value);
	}

	public static final class Read<T> extends Action<T> {

		public final int bytes;

		public Read(int bytes) {
			this.bytes = bytes;
		}
	}

	public static class Stop<T> extends Action<T> {
		public final T value;

		public Stop(T value) {
			this.value = value;
		}
	}
}
