package io.escriba.store.file;

import io.escriba.functional.Callback0;
import io.escriba.functional.Callback2;
import io.escriba.functional.Callback3;
import io.escriba.store.Store;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;

public class Context implements AutoCloseable {
	protected AsynchronousFileChannel channel;
	protected boolean closed = false;
	protected Closer closer = new Closer(this);
	private final FileContextable context;
	protected final Store.Fail fail;
	protected FileLock lock;
	// User code
	protected final Callback3<A, Callback2<ByteBuffer, Long>, Callback0> userCode;

	public Context(FileContextable context, FileStoreCollection collection, String key) {
		this.context = context;

		File file = new File(collection.directory, collection.valueIdGen.generate(key));
		File parent = file.getParentFile();
	}

//	public Context(FileStoreCollection collection, String key, Callback3<A, Callback2<ByteBuffer, Long>, Callback0> userCode, Store.Fail fail) {
//		this.userCode = userCode;
//		this.fail = fail;
//
//		File file = new File(collection.directory, collection.valueIdGen.generate(key));
//		File parent = file.getParentFile();
//
//		if (!parent.exists())
//			try {
//				// TODO: Criar arquivo de controle
//				parent.mkdirs();
//			} catch (Exception e) {
//				callFail(e);
//			}
//
//		if (closed == false) {
//			try {
//				channel = openChannel(file);
//			} catch (IOException e) {
//				callFail(e);
//			}
//		}
//	}

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

	public void lock() {
		channel.lock(null, new GetLocker(this));
	}

	public void locked(FileLock lock) {
		this.lock = lock;
		context.ready();
	}

	protected abstract AsynchronousFileChannel openChannel(File file) throws IOException;
}
