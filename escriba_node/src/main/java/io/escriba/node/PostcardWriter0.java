package io.escriba.node;

import java.nio.ByteBuffer;

public interface PostcardWriter0<T> {
	ByteBuffer apply(T value);
}
