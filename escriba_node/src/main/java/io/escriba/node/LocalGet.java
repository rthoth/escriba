package io.escriba.node;

import io.escriba.DataEntry;

import java.nio.ByteBuffer;

import static java.lang.Math.min;

public class LocalGet<T> extends Get<T> {
	public LocalGet(Postcard postcard, String key, int initialSize, PostcardReader<T> reader) throws Exception {
		super(postcard, key, initialSize, reader);

		final ByteBuffer[] localBuffer = {null};
		final DataEntry[] localEntry = {null};
		final T[] ret = (T[]) new Object[]{null};
		final long[] total = {0};

		postcard.store().collection(postcard.collection(), false).get(key)
			.ready((entry, control) -> {

				localEntry[0] = entry;
				localBuffer[0] = ByteBuffer.allocate((int) min(initialSize, entry.size));
				control.read(localBuffer[0]);

			})
			.read((bytes, buf, control) -> {

				total[0] += bytes;

				if (buf.hasRemaining())
					control.read(buf);
				else {
					buf.flip();
					Action<T> action = reader.apply(total[0], localEntry[0], buf);

					if (action instanceof Action.Read) {
						int read = ((Action.Read) action).bytes;

						if (read + total[0] > localEntry[0].size)
							read = (int) (localEntry[0].size - total[0]);

						if (read == 0)
							throw new IllegalStateException(String.format("Entry [%s] in [%s]", key, postcard.collection()));

						if (localBuffer[0].capacity() < read)
							localBuffer[0] = ByteBuffer.allocate(read);
						else
							localBuffer[0].limit(read).rewind();

						control.read(localBuffer[0]);
					} else if (action instanceof Action.Stop) {
						ret[0] = ((Action.Stop<T>) action).value;
						control.close();
					}
				}
			})
			.error(error -> completable.completeExceptionally(error))
			.success(() -> completable.complete(ret[0]))
			.start();
	}
}
