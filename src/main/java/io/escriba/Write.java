package io.escriba;

import io.escriba.functional.Callback2;

import java.nio.ByteBuffer;

public interface Write extends Callback2<ByteBuffer, Long> {
	@Override
	void apply(ByteBuffer buffer, Long position);
}
