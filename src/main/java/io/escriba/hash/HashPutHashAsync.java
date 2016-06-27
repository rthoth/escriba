package io.escriba.hash;

import io.escriba.Close;
import io.escriba.Data;
import io.escriba.Put;
import io.escriba.Write;
import io.escriba.functional.T2;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;

public class HashPutHashAsync extends HashAsync<Put.PutHandler> implements Close {
	private final HashPut put;
	private Writer write;

	public HashPutHashAsync(HashPut put, Put.PutHandler handler) throws Exception {
		super(handler);
		this.put = put;
		lock();
	}

	@Override
	public void apply() {
		close(true);
	}

	@Override
	protected Path getPath(Data data) {
		return put.getPath(data);
	}

	@Override
	protected Data loadData() {
		return this.put.data();
	}

	@Override
	protected void locked() {
		try {
			write = new Writer(this);
			handler.ready(write, this);
		} catch (Exception e) {
			error(e);
		}
	}

	private void written(int total, ByteBuffer buffer) {
		if (channel != null && channel.isOpen())
			try {
				handler.written(total, buffer, write, this);
			} catch (Exception e) {
				error(e);
			}
	}

	private static class Writer implements Write {

		private static final CompletionHandler<Integer, T2<HashPutHashAsync, ByteBuffer>> COMPLETE_HANDLER = new CompletionHandler<Integer, T2<HashPutHashAsync, ByteBuffer>>() {
			@Override
			public void completed(Integer result, T2<HashPutHashAsync, ByteBuffer> attachment) {
				attachment.a.written(result, attachment.b);
			}

			@Override
			public void failed(Throwable throwable, T2<HashPutHashAsync, ByteBuffer> attachment) {
				attachment.a.error(throwable);

			}
		};
		private final HashPutHashAsync put;

		public Writer(HashPutHashAsync put) {
			this.put = put;
		}

		@Override
		public void apply(ByteBuffer buffer, Long position) {
			if (put.channel != null && put.channel.isOpen())
				put.channel.write(buffer, position, T2.of(put, buffer), COMPLETE_HANDLER);
			else
				put.error(new IllegalStateException("Channel has been closed!"));
		}
	}
}
