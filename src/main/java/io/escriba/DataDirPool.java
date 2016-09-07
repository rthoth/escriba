package io.escriba;

public interface DataDirPool<T extends DataDirPool> {

	T copy();

	DataDir get(int index);

	int nextIndex();

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

			for (int i = 0; i < dataDirs.length; i++) {
				final DataDir dataDir = new DataDir(dataDirs[i].path, dataDirs[i].weight / min);
				total += dataDir.weight;
				entries[i] = T2.of(total - 1, dataDir);
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
			if (index > -1 && index < entries.length)
				return entries[index].b;
			else
				throw new EscribaException.IllegalArgument("Index out of range!");
		}

		@Override
		public int nextIndex() {
			synchronized (this) {
				position = (position + 1) % total;
			}

//			for (T2<Integer, DataDir> entry : entries) {
//				if (position <= entry.a)
//					return entry.a;
//			}

			for (int i = 0; i < entries.length; i++)
				if (position <= entries[i].a)
					return i;

			throw new EscribaException.Unexpected("DataDirPool.RoundRobin");
		}
	}
}
