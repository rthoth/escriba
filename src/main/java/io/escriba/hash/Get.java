package io.escriba.hash;

import io.escriba.*;
import io.escriba.DataEntry.Status;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class Get implements Close {
	private FileChannel channel;
	private final HashCollection collection;
	private DataEntry entry;
	private final ErrorHandler errorHandler;
	private final String key;
	private Read read;
	private final Getter.ReadHandler readHandler;
	private final Getter.ReadyHandler readyHandler;

	public Get(HashCollection collection, String key, Getter.ReadyHandler readyHandler, Getter.ReadHandler readHandler, ErrorHandler errorHandler) {
		this.collection = collection;
		this.key = key;
		this.readyHandler = readyHandler;
		this.readHandler = readHandler;
		this.errorHandler = errorHandler;

		entry = collection.getEntry(key);

		if (entry == null)
			throw new EscribaException.NotFound(key + " in collection " + collection.name);

		if (entry.status != Status.Ok)
			throw new EscribaException.IllegalState(key + "in collection " + collection.name + " has status equals to " + entry.status.name());

		if (readHandler != null)
			collection.executor.submit(this::openFile);
		else
			throw new EscribaException.IllegalArgument("The ReadHandler must be defined!");
	}

	@Override
	public void apply() throws Exception {
		try {

			Date date = new Date();

			entry = entry.copy()
				.access(date)
				.end()
			;

			collection.updateEntry(key, entry);

			close0();
		} catch (Exception e) {
			error(e);
		}
	}

	private void close0() throws IOException {
		if (channel != null && channel.isOpen()) {
			channel.close();
			channel = null;
		}
	}

	private void closeQuietly() {
		try {
			close0();
		} catch (Exception e) {
			// TODO: Log?
		}
	}

	private void error(Throwable throwable) {
		if (isClose())
			return;

		closeQuietly();

		if (errorHandler != null)
			try {
				errorHandler.apply(throwable);
			} catch (Exception e) {
				// TODO: Log?
			}
	}

	private boolean isClose() {
		return channel == null || !channel.isOpen();
	}

	private void openFile() {
		try {
			channel = FileChannel.open(collection.getPath(key), StandardOpenOption.READ);
		} catch (Exception e) {
			if (errorHandler != null)
				try {
					errorHandler.apply(e);
				} catch (Exception e1) {
					// TODO: Log?
				}
			return;
		}

		read = buffer -> {
			//noinspection CodeBlock2Expr
			collection.executor.submit(() -> {
				readFile(buffer);
			});
		};

		try {
			if (readyHandler != null)
				readyHandler.apply(entry, read, this);
		} catch (Exception e) {
			error(e);
		}
	}

	private void readFile(ByteBuffer buffer) {
		if (isClose())
			return;

		int w;
		try {
			w = channel.read(buffer);
		} catch (Exception e) {
			error(e);
			return;
		}

		try {
			readHandler.apply(w, buffer, read, this);
		} catch (Exception e) {
			error(e);
		}
	}
}
