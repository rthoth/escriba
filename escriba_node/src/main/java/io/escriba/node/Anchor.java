package io.escriba.node;

import io.escriba.server.Server;

public class Anchor {

	private final String host;
	private final int port;

	public Anchor(Server server) {
		host = server.listenAddress().getHostName();
		port = server.listenAddress().getPort();
	}
}
