package io.escriba;

import io.escriba.EscribaException.IllegalState;
import io.escriba.EscribaException.Unexpected;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

public class FileDataChannel implements DataChannel {
	private final FileChannel channel;
	private final DataEntry entry;

	public FileDataChannel(DataEntry entry, File diretory) {
		this.entry = entry;
		try {
			this.channel = FileChannel.open(Paths.get(diretory.getAbsolutePath(), entry.path));
		} catch (IOException e) {
			throw new Unexpected(e);
		}

		long fileSize;

		try {
			fileSize = this.channel.size();
		} catch (IOException e) {
			throw new Unexpected(e);
		}

		if (entry.size != fileSize)
			throw new IllegalState("DataEntry.size is invalid!");
	}

	@Override
	public void close() throws IOException {
		this.channel.close();
	}

	@Override
	public boolean isOpen() {
		return this.channel.isOpen();
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return this.channel.read(dst);
	}

	@Override
	public long size() {
		return this.entry.size;
	}
}
