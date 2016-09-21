package io.escriba.node;

public class RemoteGet<T> extends Get<T> {
	public RemoteGet(Postcard postcard, String key, int initialSize, PostcardReader<T> reader) {
		super(postcard, key, initialSize, reader);
	}
}
