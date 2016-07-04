package io.escriba;

public interface Collection {

	Getter get(String key);

	Putter put(String key, String mediaType);

}
