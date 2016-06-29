package io.escriba;

import java.nio.file.NoSuchFileException;

public class EscribaException extends RuntimeException {

	public EscribaException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class IllegalState extends RuntimeException {
		public IllegalState(String message) {
			super(message);
		}
	}

	public static class NoValue extends EscribaException {

		public NoValue(String key, String collection, NoSuchFileException noFileEx) {
			super(key + " in " + collection, noFileEx);
		}
	}

	public static class NotFound extends RuntimeException {
		public NotFound(String message) {
			super(message);
		}
	}

	public static class Unexpected extends RuntimeException {
		public Unexpected(String message, Throwable cause) {
			super(message, cause);
		}

		public Unexpected(Throwable cause) {
			super(cause);
		}
	}
}
