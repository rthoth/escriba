package io.escriba.store.file;

import io.escriba.functional.Callback0;
import io.escriba.store.file.Context;

public class Closer<A> implements Callback0 {
	private final Context<A> context;

	public Closer(Context<A> context) {
		this.context = context;
	}

	@Override
	public void apply() throws Exception {
		context.close(true);
	}
}
