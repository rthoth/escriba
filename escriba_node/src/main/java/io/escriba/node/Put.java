package io.escriba.node;

import java.util.concurrent.Future;

public abstract class Put {
	public abstract Future<Postcard> start();
}
