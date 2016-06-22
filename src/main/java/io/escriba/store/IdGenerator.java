package io.escriba.store;

import java.io.File;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class IdGenerator {

	public static final int RADIX = 32;
	private static final char SEPARATOR = File.separatorChar;
	private final int parts;

	public IdGenerator() {
		this(3);
	}

	public IdGenerator(int parts) {
		this.parts = parts;
	}

	public String generate(String key) {
		byte[] bytes = key.getBytes();

		Checksum checksum = new CRC32();
		String[] strings = new String[parts];
		int i = 0, limit;

		if (bytes.length > parts) {

			int inc = bytes.length / parts;
			int mod = bytes.length % parts;
			limit = parts - 1;
			int off;

			for (off = 0; i < limit; i++, off += inc) {
				checksum.update(bytes, off, inc);
				strings[i] = Long.toString(checksum.getValue(), RADIX);
			}

			checksum.update(bytes, off, inc + mod);
			strings[i] = Long.toString(checksum.getValue(), RADIX);
		} else {
			checksum.update(bytes, 0, bytes.length);
			strings[0] = Long.toString(checksum.getValue(), RADIX);
			for (i = 1; i < strings.length; i++) {
				strings[i] = strings[0];
			}
		}

		StringBuilder sb = new StringBuilder();

		for (i = 0, limit = parts - 1; i < limit; i++) {
			sb.append(strings[i]).append(SEPARATOR);
		}

		return sb.append(strings[parts - 1]).toString();
	}

}
