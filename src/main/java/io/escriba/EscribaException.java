package io.escriba;

import java.nio.file.NoSuchFileException;

public class EscribaException extends RuntimeException {

	public EscribaException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class NoValue extends EscribaException {

		public NoValue(String key, String collection, NoSuchFileException noFileEx) {
			super(key + " in " + collection, noFileEx);
		}
	}
}
