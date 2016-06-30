package io.escriba;

import java.nio.channels.ReadableByteChannel;

public interface DataChannel extends ReadableByteChannel {
	String mediaType();

	long size();
}
