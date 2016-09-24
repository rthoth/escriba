package io.escriba.node;

import io.escriba.DataEntry;

import java.nio.ByteBuffer;

public interface PostcardReader0<T> {
	T apply(DataEntry entry, ByteBuffer buffer);
}
