package test;

import io.escriba.functional.Callback0;

public class Wait {

	private Throwable fail = null;
	private boolean wait = true;

	public void attempt(Callback0 block) {
		try {
			block.apply();
		} catch (Throwable e) {
			fail = e;
		}
	}

	public void free() {
		wait = false;
	}

	public void sleep() {
		while (wait && fail == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}

		if (fail != null)
			throw new RuntimeException(fail);
	}
}
