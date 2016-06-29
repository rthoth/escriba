package test;

import io.escriba.functional.Callback0;

public class Wait {

	private Throwable fail;
	private boolean wait = true;

	public void attempt(Callback0 block) {
		try {
			block.apply();
		} catch (Throwable e) {
			this.fail = e;
		}
	}

	public void free() {
		this.wait = false;
	}

	public void sleep() {
		while (this.wait && this.fail == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}

		if (this.fail != null)
			throw new RuntimeException(this.fail);
	}
}
