package io.escriba.server;

import io.escriba.Func;

public abstract class Command {
	public final Func.T2<String, String> tuple;

	protected Command(Func.T2<String, String> tuple) {
		this.tuple = tuple;
	}

	public static Put put(Func.T2<String, String> tuple) {
		return new Put(tuple);
	}

	public static Get get(Func.T2<String, String> tuple) {
		return new Get(tuple);
	}

	public static class Put extends Command {

		public Put(Func.T2<String, String> tuple) {
			super(tuple);
		}
	}

	public static class Get extends Command {
		public Get(Func.T2<String, String> tuple) {
			super(tuple);
		}
	}
}
