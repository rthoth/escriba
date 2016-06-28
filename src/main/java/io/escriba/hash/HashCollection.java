package io.escriba.hash;

import io.escriba.*;
import org.mapdb.Atomic;
import org.mapdb.HTreeMap;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import static java.io.File.separator;
import static java.lang.Integer.toHexString;

public class HashCollection implements Collection {
	public static final double X = 1e3;
	public static final double XY = 1e6;
	private final Atomic.Long atomic;
	private final File diretory;
	final HTreeMap<String, Data> map;
	final String name;

	public HashCollection(String name, HTreeMap<String, Data> map, File directory, Atomic.Long atomic) {
		this.name = name;
		this.map = map;
		this.diretory = directory;
		this.atomic = atomic;

		if (!directory.exists())
			directory.mkdirs();
	}

	@Override
	public Get get(String key, Get.ReadyHandler readyHandler, Get.ReadHandler readHandler) throws Exception {
		return new AsyncGet(this, key, readyHandler, readHandler, null);
	}

	@Override
	public Get get(String key, Get.ReadyHandler readyHandler, Get.ReadHandler readHandler, ErrorHandler errorHandler) throws Exception {
		return new AsyncGet(this, key, readyHandler, readHandler, errorHandler);
	}

	@Override
	public FileChannel getChannel(String key) throws Exception {
		return FileChannel.open(getFile(key).toPath(), StandardOpenOption.READ);
	}

	File getFile(String key) {
		Data data = map.get(key);

		if (data == null) {
			data = new Data(nextPath());
			map.put(key, data);
		}

		switch (data.status) {
			case Deleting:
				new File(diretory, data.path).delete();
				data = data.path(nextPath()).status(Data.Status.Updating);
				map.put(key, data);
				break;
		}

		File file = new File(diretory, data.path);
		File parent = file.getParentFile();
		if (!parent.exists())
			parent.mkdirs();

		return file;
	}

	private String nextPath() {
		long next;
		synchronized (atomic) {
			next = atomic.getAndIncrement();
		}

		int z = (int) (next / XY);
		int y = (int) ((next % XY) / X);
		int x = (int) (next % X);

		return toHexString(x) + separator + toHexString(y) + separator + toHexString(z);
	}

	@Override
	public Put put(String key, Put.ReadyHandler readyHandler, Put.WrittenHandler writtenHandler) throws Exception {
		return new AsyncPut(this, key, readyHandler, writtenHandler, null);
	}

	@Override
	public Put put(String key, Put.ReadyHandler readyHandler, Put.WrittenHandler writtenHandler, ErrorHandler errorHandler) throws Exception {
		return new AsyncPut(this, key, readyHandler, writtenHandler, errorHandler);
	}
}
