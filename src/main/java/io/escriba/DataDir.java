package io.escriba;

import java.io.File;

/**
 * Each dataDir
 */
public class DataDir {

	public final File dir;
	public final int index;
	public final int weight;

	public DataDir(File dir, int weight, int index) {
		this.dir = dir;
		this.weight = weight;
		this.index = index;
	}

	public static DataDir[] of(T2<Integer, File> t2, T2<Integer, File>... t2s) {
		DataDir[] dataDirs = new DataDir[t2s.length + 1];
		dataDirs[0] = new DataDir(t2.b, t2.a, 0);

		int i = 1;
		for (T2<Integer, File> t : t2s) {
			dataDirs[i] = new DataDir(t.b, t.a, i);
			i++;
		}

		return dataDirs;
	}
}
