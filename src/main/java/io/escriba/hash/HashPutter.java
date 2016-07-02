package io.escriba.hash;

import io.escriba.ErrorHandler;
import io.escriba.Putter;

public class HashPutter implements Putter {

	private final HashCollection collection;
	private ErrorHandler errorHandler;
	private final String key;
	private final String mediaType;
	private ReadyHandler readyHandler;
	private WrittenHandler writtenHandler;

	public HashPutter(HashCollection collection, String key, String mediaType) {
		this.collection = collection;
		this.key = key;
		this.mediaType = mediaType;
	}

	@Override
	public Putter error(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		return this;
	}

	@Override
	public Putter ready(ReadyHandler readyHandler) {
		this.readyHandler = readyHandler;
		return this;
	}

	@Override
	public void start() {
		new Put(collection, key, mediaType, readyHandler, writtenHandler, errorHandler);
	}

	@Override
	public Putter written(WrittenHandler writtenHandler) {
		this.writtenHandler = writtenHandler;
		return this;
	}
}
