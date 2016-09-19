package io.escriba.node;

import java.nio.ByteBuffer;

public interface PostcardWriter<T> {
	ByteBuffer apply(T value) throws Exception;
}
