package io.escriba;

public abstract class EscribaException extends RuntimeException {

	public EscribaException(String message, Throwable cause) {
		super(message, cause);
	}

	public EscribaException(String message) {
		super(message);
	}

	public EscribaException(Throwable cause) {
		super(cause);
	}

	public static class IllegalArgument extends EscribaException {
		public IllegalArgument(String message) {
			super(message);
		}
	}

	public static class IllegalState extends RuntimeException {
		public IllegalState(String message) {
			super(message);
		}
	}

	public static class NoValue extends EscribaException {

		public NoValue(String message) {
			super(message);
		}
	}

	public static class NotFound extends EscribaException {
		public NotFound(String message) {
			super(message);
		}
	}

	public static class Unexpected extends EscribaException {
		public Unexpected(String message, Throwable cause) {
			super(message, cause);
		}

		public Unexpected(Throwable cause) {
			super(cause);
		}

		public Unexpected(String message) {
			super(message);
		}
	}
}
