package io.escriba.server;

public class Config {
	public final int dispatchers;
	public final int workers;

	public Config(int dispatchers, int workers) {
		this.dispatchers = dispatchers;
		this.workers = workers;
	}
}
