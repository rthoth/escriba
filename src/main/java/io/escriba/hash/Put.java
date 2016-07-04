package io.escriba.hash;

import io.escriba.*;
import io.escriba.DataEntry.Status;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class Put implements Close {

	private FileChannel channel;
	private final HashCollection collection;
	private DataEntry entry;
	private final ErrorHandler errorHandler;
	private final String key;
	private FileLock lock;
	private final String mediaType;
	private final Putter.ReadyHandler readyHandler;
	private Write write;
	private final Putter.WrittenHandler writtenHandler;

	public Put(HashCollection collection, String key, String mediaType, Putter.ReadyHandler readyHandler, Putter.WrittenHandler writtenHandler, ErrorHandler errorHandler) {
		this.collection = collection;
		this.key = key;
		this.mediaType = mediaType;
		this.readyHandler = readyHandler;
		this.writtenHandler = writtenHandler;
		this.errorHandler = errorHandler;

		if (writtenHandler != null)
			collection.executor.submit(this::lockFile);
		else
			throw new EscribaException.IllegalArgument("The writtenHandler must be defined");
	}

	@Override
	public void apply() throws Exception {
		if (isClosed())
			return;

		try {
			Date date = new Date();

			entry = entry.copy()
				.size(channel.size())
				.status(Status.Ok)
				.mediaType(mediaType)
				.update(date)
				.access(date)
				.copy()
			;

			close0();
			collection.update(key, entry);
		} catch (Exception e) {
			if (errorHandler != null)
				try {
					errorHandler.apply(e);
				} catch (Exception e1) {
					// TODO: Log?
				}
		}
	}

	private void close0() throws Exception {
		if (lock != null) {
			lock.close();
			lock = null;
		}

		if (channel != null && channel.isOpen()) {
			channel.close();
			channel = null;
		}
	}

	private void closeQuietly() {
		try {
			close0();
		} catch (Exception e) {
			// TODO: Log e?
		}
	}

	private void error(Exception throwable) {
		if (isClosed())
			return;

		closeQuietly();

		if (errorHandler != null)
			try {
				errorHandler.apply(throwable);
			} catch (Exception e) {
				// TODO: Log?
			}
	}

	private boolean isClosed() {
		return channel == null || !channel.isOpen();
	}

	private void lockFile() {

		try {

			entry = collection.getOrCreateEntry(key);

			if (entry.status != Status.Creating)
				collection.update(key, entry = entry.status(Status.Updating));

			Path path = collection.getPath(key);

			File parent = path.getParent().toFile();

			if (!parent.exists())
				parent.mkdirs();

			channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			lock = channel.lock();
		} catch (Exception e) {
			if (errorHandler != null)
				try {
					errorHandler.apply(e);
				} catch (Exception e1) {
					// TODO: Log?
				}
			return;
		}

		//noinspection CodeBlock2Expr
		write = buffer -> {
			if (isClosed())
				return;
			//noinspection CodeBlock2Expr
			collection.executor.submit(() -> {
				write(buffer);
			});
		};

		try {
			if (readyHandler != null)
				readyHandler.apply(write, this);
			else
				writtenHandler.apply(0, null, write, this);
		} catch (Exception e) {
			error(e);
		}
	}

	private void write(ByteBuffer buffer) {
		if (isClosed())
			return;

		int written;
		try {
			written = channel.write(buffer);
		} catch (Exception e) {
			error(e);
			return;
		}

		written(written, buffer);
	}

	private void written(int written, ByteBuffer buffer) {
		if (isClosed())
			return;

		try {
			writtenHandler.apply(written, buffer, write, this);
		} catch (Exception e) {
			error(e);
		}
	}
}
