package io.escriba;

public interface DataDirPool<T extends DataDirPool> {

	T copy();

	DataDir get(int index);

	DataDir next();

	class RoundRobin implements DataDirPool<RoundRobin> {
		private final T2<Integer, DataDir>[] entries;
		private int position = -1;
		private final int total;

		public RoundRobin(DataDir[] dataDirs) {
			int min = Integer.MAX_VALUE, total = 0;

			for (DataDir dataDir : dataDirs) {
				if (dataDir.weight < min)
					min = dataDir.weight;
			}

			entries = new T2[dataDirs.length];
			int i = 0, ceil = -1;
			for (DataDir dataDir : dataDirs) {
				dataDir = new DataDir(dataDir.path, dataDir.weight / min, i);
				total += dataDir.weight;
				entries[i] = new T2(ceil + dataDir.weight, dataDir);
				ceil = entries[i++].a;
			}

			this.total = total;
		}

		private RoundRobin(T2<Integer, DataDir>[] entries, int total) {
			this.entries = entries;
			this.total = total;
		}

		@Override
		public RoundRobin copy() {
			return new RoundRobin(entries, total);
		}

		@Override
		public DataDir get(int index) {
			if (index < entries.length)
				return entries[index].b;
			else
				throw new EscribaException.IllegalArgument("Index out of range!");
		}

		@Override
		public DataDir next() {
			synchronized (this) {
				position = (position + 1) % total;
			}

			for (T2<Integer, DataDir> entry : entries) {
				if (position <= entry.a)
					return entry.b;
			}

			throw new EscribaException.Unexpected("DataDirPool.RoundRobin");
		}
	}
}
