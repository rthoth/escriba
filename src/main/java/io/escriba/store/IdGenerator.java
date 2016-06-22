package io.escriba.store;

import java.io.File;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class IdGenerator {

	private static final char SEPARATOR = File.separatorChar;
	private final short parts;

	public IdGenerator() {
		this((short) 3);
	}

	public IdGenerator(short parts) {
		this.parts = parts;
	}

	public String generate(String key) {
		byte[] bytes = key.getBytes();

		Checksum adler32 = new CRC32();
		String[] strings = new String[parts];
		short i = 0, limit;

		if (bytes.length > parts) {

			short inc = (short) (bytes.length / parts);
			short mod = (short) (bytes.length % parts);
			limit = (short) (parts - 1);
			short off;

			for (off = 0; i < limit; i++, off += inc) {
				adler32.update(bytes, off, inc);
				strings[i] = Long.toHexString(adler32.getValue());
			}

			adler32.update(bytes, off, inc + mod);
			strings[i] = Long.toHexString(adler32.getValue());
		} else {
			adler32.update(bytes, 0, bytes.length);
			strings[0] = Long.toHexString(adler32.getValue());
			for (i = 1; i < strings.length; i++) {
				strings[i] = strings[0];
			}
		}

		StringBuilder sb = new StringBuilder();

		for (i = 0, limit = (short) (parts - 1); i < limit; i++) {
			sb.append(strings[i]).append(SEPARATOR);
		}

		return sb.append(strings[parts - 1]).toString();
	}

}
