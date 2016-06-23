package io.escriba.store.file;

import io.escriba.Func;
import io.escriba.functional.T2;
import io.escriba.store.Close;
import io.escriba.store.Get;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;

public class AsyncGet extends AsyncOperation<FileGet, Get.GetHandler> implements Close {
	private final Read read;

	public AsyncGet(FileGet fileGet, Get.GetHandler handler) throws IOException {
		super(fileGet, fileGet.asyncChannel(StandardOpenOption.READ), handler);
		read = new Read(this);
		ready();
	}

	@Override
	public void apply() throws Exception {
		close(true);
	}

	private void data(int total, ByteBuffer buffer) {
		try {
			handler.data(total, buffer, read, this);
		} catch (Exception e) {
			close(false);
			error(e);
		}
	}

	@Override
	protected void locked() {

	}

	private void ready() {
		try {
			handler.ready(read, this);
		} catch (Exception e) {
			close(false);
			error(e);
		}
	}

	private static class Read implements Get.Read {
		private static final CompletionHandler<Integer, T2<AsyncGet, ByteBuffer>> READ_HANDLER = new CompletionHandler<Integer, T2<AsyncGet, ByteBuffer>>() {
			@Override
			public void completed(Integer result, T2<AsyncGet, ByteBuffer> attachment) {
				attachment.a.data(result, attachment.b);
			}

			@Override
			public void failed(Throwable throwable, T2<AsyncGet, ByteBuffer> attachment) {
				attachment.a.close(false);
				attachment.a.error(throwable);
			}
		};
		private final AsyncGet get;

		public Read(AsyncGet get) {
			this.get = get;
		}

		@Override
		public void apply(ByteBuffer buffer, Long position) throws Exception {
			get.channel.read(buffer, position, Func.t2(get, buffer), READ_HANDLER);
		}
	}
}
