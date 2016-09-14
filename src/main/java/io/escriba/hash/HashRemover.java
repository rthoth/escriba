package io.escriba.hash;

import io.escriba.DataEntry;
import io.escriba.ErrorHandler;
import io.escriba.Remover;

import java.util.concurrent.Future;

public class HashRemover implements Remover {
	private final HashCollection collection;
	private ErrorHandler errorHandler;
	private final String key;
	private RemovedHandler removedHandler;

	public HashRemover(HashCollection collection, String key) {
		this.collection = collection;
		this.key = key;
	}

	@Override
	public Remover complete(RemovedHandler removedHandler) {
		this.removedHandler = removedHandler;
		return this;
	}

	@Override
	public Remover error(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		return this;
	}

	@Override
	public Future<DataEntry> start() {
		return new Remove(collection, key, removedHandler, errorHandler).future();
	}
}
