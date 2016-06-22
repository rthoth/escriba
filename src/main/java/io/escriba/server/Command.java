package io.escriba.server;

import io.escriba.functional.T2;

public abstract class Command {
	public final T2<String, String> tuple;

	protected Command(T2<String, String> tuple) {
		this.tuple = tuple;
	}

	public static Put put(T2<String, String> tuple) {
		return new Put(tuple);
	}

	public static Get get(T2<String, String> tuple) {
		return new Get(tuple);
	}

	public static class Put extends Command {

		public Put(T2<String, String> tuple) {
			super(tuple);
		}
	}

	public static class Get extends Command {
		public Get(T2<String, String> tuple) {
			super(tuple);
		}
	}
}
