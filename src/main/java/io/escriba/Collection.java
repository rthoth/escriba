package io.escriba;

public interface Collection {

	DataChannel getChannel(String key);

	Putter put(String key, String mediaType);

}
