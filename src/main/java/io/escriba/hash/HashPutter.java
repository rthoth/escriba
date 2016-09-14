package io.escriba.hash;

import io.escriba.DataEntry;
import io.escriba.ErrorHandler;
import io.escriba.Putter;
import io.escriba.SuccessHandler;

import java.util.concurrent.Future;

public class HashPutter implements Putter {

	private final HashCollection collection;
	private ErrorHandler errorHandler;
	private final String key;
	private final String mediaType;
	private ReadyHandler readyHandler;
	private SuccessHandler successHandler;
	private WrittenHandler writtenHandler;

	public HashPutter(HashCollection collection, String key, String mediaType) {
		this.collection = collection;
		this.key = key;
		this.mediaType = mediaType;
	}

	@Override
	public Future<DataEntry> start() {
		return new Put(collection, key, mediaType, readyHandler, writtenHandler, errorHandler, successHandler).future();
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
	public Putter success(SuccessHandler successHandler) {
		this.successHandler = successHandler;
		return this;
	}

	@Override
	public Putter written(WrittenHandler writtenHandler) {
		this.writtenHandler = writtenHandler;
		return this;
	}
}
