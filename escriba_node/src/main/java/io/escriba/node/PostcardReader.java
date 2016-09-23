package io.escriba.node;

import io.escriba.DataEntry;

import java.nio.ByteBuffer;

public interface PostcardReader<T> {
	ReadAction<T> apply(long total, DataEntry entry, ByteBuffer buffer);
}
