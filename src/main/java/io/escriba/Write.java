package io.escriba;

import java.nio.ByteBuffer;

public interface Write {
	void apply(ByteBuffer buffer);
}
