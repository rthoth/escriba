package io.escriba;

import java.nio.file.Path;

public interface DataDirPool<T extends DataDirPool> {

	T copy();

	Path get(int index);

	int nextIndex();

	class Fixed implements DataDirPool<Fixed> {
		private final Path path;

		public Fixed(Path path) {
			this.path = path;

		}

		@Override
		public Fixed copy() {
			return this;
		}

		@Override
		public Path get(int index) {
			return path;
		}

		@Override
		public int nextIndex() {
			return 0;
		}
	}

	class RoundRobin implements DataDirPool<RoundRobin> {
		private final T2<Integer, Path>[] paths;
		private int position = -1;
		private final int total;

		public RoundRobin(T2<Path, Integer>[] paths) {

			int min = Integer.MAX_VALUE, total = 0;

			for (T2<Path, Integer> tuple : paths) {
				if (tuple.b <= 0)
					throw new EscribaException.IllegalArgument("Invalid weight: " + tuple);
				else if (tuple.b < min)
					min = tuple.b;
			}

			this.paths = new T2[paths.length];

			for (int i = 0; i < paths.length; i++) {
				int weight = paths[i].b / min;
				total += weight;
				this.paths[i] = T2.of(weight, paths[i].a);
			}

			this.total = total;
		}

		private RoundRobin(T2<Integer, Path>[] paths, int total) {
			this.paths = paths;
			this.total = total;
		}

		@Override
		public RoundRobin copy() {
			return new RoundRobin(paths, total);
		}

		@Override
		public Path get(int index) {
			if (index > -1 && index < paths.length)
				return paths[index].b;
			else
				throw new EscribaException.IllegalArgument("Index out of range!");
		}

		@Override
		public int nextIndex() {
			synchronized (this) {
				position = (position + 1) % total;
			}

//			for (T2<Integer, DataDir> entry : paths) {
//				if (position <= entry.a)
//					return entry.a;
//			}

			for (int i = 0; i < paths.length; i++)
				if (position <= paths[i].a)
					return i;

			throw new EscribaException.Unexpected("DataDirPool.RoundRobin");
		}
	}
}
