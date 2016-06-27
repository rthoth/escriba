package io.escriba;

import java.nio.ByteBuffer;

public interface Read {
	void apply(ByteBuffer buffer, long position) throws Exception;
}
