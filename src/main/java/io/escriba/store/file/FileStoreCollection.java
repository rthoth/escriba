package io.escriba.store.file;

import io.escriba.Func;
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
	}

	@Override
	public void put(String key, Store.Putter putter, Store.Fail fail) {
		new PutContext(key, putter, fail);
	}

	private class Closer implements Func.C0 {
		private final PutContext context;

		public Closer(PutContext context) {
			this.context = context;
		}

		@Override
		public void apply() throws Exception {
			this.context.close0(true);
		}
	}

	private class Locker implements CompletionHandler<FileLock, Object> {
		private final PutContext context;

		public Locker(PutContext context) {
			this.context = context;
		}

		@Override
		public void completed(FileLock result, Object attachment) {
			this.context.lock = result;
			this.context.invokePutter(null);
		}

		@Override
		public void failed(Throwable exc, Object attachment) {
			this.context.invokeFail(exc);
		}
	}

	private class PutContext {
		private AsynchronousFileChannel channel = null;
		private Func.C0 close = new Closer(this);
		private boolean closed = false;
		private final Store.Fail fail;
		private final File file;
		public FileLock lock;
		private final Store.Putter putter;
		private Func.C2<ByteBuffer, Long> write = new Writer(this);

		public PutContext(String key, Store.Putter putter, Store.Fail fail) {
			this.file = new File(directory, valueIdGen.generate(key));
			this.putter = putter;
			this.fail = fail;

			if (!file.getParentFile().exists())
				try {
					this.file.getParentFile().mkdirs();
					// TODO: Criar arquivo de controle
				} catch (Exception e) {
					invokeFail(e);
				}

			if (closed == false) {
				try {
					channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				} catch (IOException e) {
					invokeFail(e);
				}
			}

			if (closed == false)
				channel.lock(null, new Locker(this));
		}

		private void close0(boolean invoke) {
			if (closed == false) {
				closed = true;
				if (lock != null)
					try {
						lock.release();
					} catch (IOException e) {
					}
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

		private void invokeFail(Throwable throwable) {
			if (closed == false) {
				close0(false);

				if (fail != null)
					try {
						fail.apply(throwable);
					} catch (Exception e) {

					}
			}
		}

		private void invokePutter(Integer writen) {
			try {
				putter.apply(writen, write, close);
			} catch (Exception e) {
				invokeFail(e);
			}
		}
	}

	private class Writer implements Func.C2<ByteBuffer, Long>, CompletionHandler<Integer, Object> {

		private final PutContext context;

		public Writer(PutContext context) {
			this.context = context;
		}

		@Override
		public void apply(ByteBuffer byteBuffer, Long position) throws Exception {
			context.channel.write(byteBuffer, position, null, this);
		}

		@Override
		public void completed(Integer result, Object attachment) {
			context.invokePutter(result);
		}

		@Override
		public void failed(Throwable exc, Object attachment) {
			context.invokeFail(exc);
		}
	}
}
