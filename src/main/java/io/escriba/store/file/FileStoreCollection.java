package io.escriba.store.file;

import io.escriba.Func;
import io.escriba.functional.Callback0;
import io.escriba.functional.Callback2;
import io.escriba.functional.Callback3;
import io.escriba.functional.T2;
import io.escriba.store.IdGenerator;
import io.escriba.store.Store;
import io.escriba.store.StoreCollection;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class FileStoreCollection extends StoreCollection {
	private final File directory;
	private final String name;
	private final IdGenerator valueIdGen;

	public FileStoreCollection(File directory, String name, IdGenerator valueIdGen) {
		this.directory = directory;
		this.name = name;
		this.valueIdGen = valueIdGen;

		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	@Override
	public void get(String key, Store.Getter getter, Store.Fail fail) {
		new GetContext(key, getter, fail);
	}

	@Override
	public void put(String key, Store.Putter putter, Store.Fail fail) {
		new PutContext(key, putter, fail);
	}

	private class Closer<A> implements Callback0 {
		private final Context<A> context;

		public Closer(Context<A> context) {
			this.context = context;
		}

		@Override
		public void apply() throws Exception {
			context.close(true);
		}
	}

	public abstract class Context<A> implements AutoCloseable {
		protected AsynchronousFileChannel channel;
		protected boolean closed = false;
		protected Closer closer = new Closer(this);
		protected final Store.Fail fail;
		protected FileLock lock;
		// User code
		protected final Callback3<A, Callback2<ByteBuffer, Long>, Callback0> userCode;

		public Context(String key, Callback3<A, Callback2<ByteBuffer, Long>, Callback0> userCode, Store.Fail fail) {
			this.userCode = userCode;
			this.fail = fail;

			File file = new File(directory, valueIdGen.generate(key));
			File parent = file.getParentFile();

			if (!parent.exists())
				try {
					// TODO: Criar arquivo de controle
					parent.mkdirs();
				} catch (Exception e) {
					callFail(e);
				}

			if (closed == false) {
				try {
					channel = openChannel(file);
				} catch (IOException e) {
					callFail(e);
				}
			}
		}

		protected void callFail(Throwable throwable) {
			if (closed == false) {
				close(false);

				if (fail != null)
					try {
						fail.apply(throwable);
					} catch (Exception e) {

					}
			}
		}

		protected void callUserCode(A a) {
			if (closed == false)
				try {
					userCode.apply(a, callback(), closer);
				} catch (Exception e) {
					callFail(e);
				}
		}

		protected abstract Callback2<ByteBuffer, Long> callback();

		@Override
		public void close() throws Exception {
			close(true);
		}

		protected void close(boolean invoke) {
			if (closed == false) {
				closed = true;

				if (channel != null && channel.isOpen())
					try {
						channel.close();
					} catch (Exception e) {
						if (invoke && fail != null)
							try {
								fail.apply(e);
							} catch (Exception e1) {
							}
					}
			}
		}

		protected abstract AsynchronousFileChannel openChannel(File file) throws IOException;
	}

	private class GetContext extends Context<T2<Integer, ByteBuffer>> {

		private final Reader reader;

		public GetContext(String key, Store.Getter getter, Store.Fail fail) {
			super(key, getter, fail);
			reader = new Reader(this);
			try {
				callUserCode(null);
			} catch (Exception e) {
				callFail(e);
			}
		}

		@Override
		protected Callback2<ByteBuffer, Long> callback() {
			return reader;
		}

		@Override
		protected AsynchronousFileChannel openChannel(File file) throws IOException {
			return AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
		}
	}

	private class GetLocker<A> implements CompletionHandler<FileLock, Object> {
		private final Context<A> context;

		public GetLocker(Context<A> context) {
			this.context = context;
		}

		@Override
		public void completed(FileLock fileLock, Object attachment) {
			context.lock = fileLock;
			context.callUserCode(null);
		}

		@Override
		public void failed(Throwable throwable, Object attachment) {
			context.callFail(throwable);
		}
	}

	private class PutContext extends Context<Integer> {
		private final Writer writer;

		public PutContext(String key, Store.Putter putter, Store.Fail fail) {
			super(key, putter, fail);

			writer = new Writer(this);

			if (closed == false)
				channel.lock(null, new GetLocker(this));
		}

		@Override
		protected Callback2<ByteBuffer, Long> callback() {
			return writer;
		}

		@Override
		protected AsynchronousFileChannel openChannel(File file) throws IOException {
			return AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
	}

	private class Reader implements Callback2<ByteBuffer, Long> {
		protected final GetContext context;

		public Reader(GetContext context) {
			this.context = context;
		}

		@Override
		public void apply(ByteBuffer buffer, Long position) throws Exception {
			context.channel.read(buffer, position, null, new ReaderHandler(context, buffer));
		}
	}

	private class ReaderHandler implements CompletionHandler<Integer, Object> {

		private final ByteBuffer buffer;
		private final GetContext context;

		public ReaderHandler(GetContext context, ByteBuffer buffer) {
			this.context = context;
			this.buffer = buffer;
		}

		@Override
		public void completed(Integer read, Object attachment) {
			context.callUserCode(Func.t2(read, buffer));
		}

		@Override
		public void failed(Throwable throwable, Object attachment) {
			context.callFail(throwable);
		}
	}

	private class Writer implements Callback2<ByteBuffer, Long>, CompletionHandler<Integer, Object> {

		private final PutContext context;

		public Writer(PutContext context) {
			this.context = context;
		}

		@Override
		public void apply(ByteBuffer buffer, Long position) throws Exception {
			context.channel.write(buffer, position, null, this);
		}

		@Override
		public void completed(Integer writen, Object attachment) {
			context.callUserCode(writen);
		}

		@Override
		public void failed(Throwable throwable, Object attachment) {
			context.callFail(throwable);
		}
	}
}
