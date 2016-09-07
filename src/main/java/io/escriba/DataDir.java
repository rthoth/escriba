package io.escriba;

import java.io.File;
import java.nio.file.Path;

/**
 * DataDir is where collection data stay.
 */
public class DataDir {

	public final Path path;
	public final int weight;

	public DataDir(Path path, int weight) {
		this.path = path.toAbsolutePath();
		this.weight = weight;
	}

	public static DataDir[] of(T2<Integer, File> t2, T2<Integer, File>... t2s) {
		DataDir[] dataDirs = new DataDir[t2s.length + 1];
		dataDirs[0] = new DataDir(t2.b.toPath(), t2.a);

		int i = 0;
		for (T2<Integer, File> t : t2s) {
			dataDirs[i++] = new DataDir(t.b.toPath(), t.a);
		}

		return dataDirs;
	}
}
