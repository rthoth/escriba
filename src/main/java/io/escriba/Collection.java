package io.escriba;

public interface Collection {

	Getter get(String key);

	@Deprecated
	DataChannel getChannel(String key);

	Putter put(String key, String mediaType);

}
