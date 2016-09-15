package io.escriba.server;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Helper to locked code
 */
public class LockedBlock {

	protected ReentrantLock lock = new ReentrantLock();

	public void locked(Block block) throws Exception {
		lock.lock();

		try {
			block.apply();
		} finally {
			lock.unlock();
		}
	}

	public interface Block {
		void apply() throws Exception;
	}
}
