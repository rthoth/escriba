package io.escriba;

public interface Collection {

	Getter get(String key);

	DataEntry getEntry(String key);

	Putter put(String key, String mediaType);

	Remover remove(String key);
}
