package io.escriba.store.file;

import io.escriba.Func;
import io.escriba.functional.T2;
import io.escriba.store.Close;
import io.escriba.store.Put;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class AsyncPut extends AsyncOperation<FilePut, Put.PutHandler> implements Close {
	private final Write write;

	public AsyncPut(FilePut put, Put.PutHandler handler) throws IOException {
		super(put, put.asyncChannel(CREATE, WRITE), handler);
		write = new Write(this);
		lock();
	}

	@Override
	public void apply() throws Exception {
		close(true);
	}

	private void data(int written, ByteBuffer buffer) {
		try {
			handler.data(written, buffer, write, this);
		} catch (Exception e) {
			close(false);
			error(e);
		}
	}

	@Override
	protected void locked() {
		try {
			handler.ready(write, this);
		} catch (Exception e) {
			close(false);
			error(e);
		}
	}

	private static class Write implements Put.Write {
		private static final CompletionHandler<Integer, T2<AsyncPut, ByteBuffer>> WRITE_HANDLER = new CompletionHandler<Integer, T2<AsyncPut, ByteBuffer>>() {
			@Override
			public void completed(Integer result, T2<AsyncPut, ByteBuffer> attachment) {
				attachment.a.data(result, attachment.b);
			}

			@Override
			public void failed(Throwable throwable, T2<AsyncPut, ByteBuffer> attachment) {
				attachment.a.close(false);
				attachment.a.error(throwable);
			}
		};

		private final AsyncPut put;

		public Write(AsyncPut put) {
			this.put = put;
		}

		@Override
		public void apply(ByteBuffer buffer, Long position) throws Exception {
			put.channel.write(buffer, position, Func.t2(put, buffer), WRITE_HANDLER);
		}
	}
}
