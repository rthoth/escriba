package io.escriba.hash;

import io.escriba.ErrorHandler;
import io.escriba.Getter;

public class HashGetter implements Getter {
	private final HashCollection collection;
	private ErrorHandler errorHandler;
	private final String key;
	private ReadHandler readHandler;
	private ReadyHandler readyHandler;

	public HashGetter(HashCollection collection, String key) {
		this.collection = collection;
		this.key = key;
	}

	@Override
	public Getter error(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		return this;
	}

	@Override
	public Getter read(ReadHandler readHandler) {
		this.readHandler = readHandler;
		return this;
	}

	@Override
	public Getter ready(ReadyHandler readyHandler) {
		this.readyHandler = readyHandler;
		return this;
	}

	@Override
	public void start() {
		new Get(collection, key, readyHandler, readHandler, errorHandler);
	}
}
