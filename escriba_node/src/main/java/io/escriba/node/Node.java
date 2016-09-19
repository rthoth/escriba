package io.escriba.node;

import io.escriba.server.Server;

@SuppressWarnings("unused")
public class Node {

	private final Anchor anchor;
	private final Server server;

	public Node(Server server) {
		this.server = server;
		this.anchor = new Anchor(server);
	}

	public Postcard postcard(String collection) {
		return new Postcard(collection, server);
	}
}
