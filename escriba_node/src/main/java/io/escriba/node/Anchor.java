package io.escriba.node;

import io.escriba.server.Server;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Anchor implements Comparable<Anchor>, Serializable {

	private static final long serialVersionUID = 100L;

	private final String host;
	private final int port;

	public Anchor(Server server) {
		host = server.listenAddress().getHostName();
		port = server.listenAddress().getPort();
	}

	public Anchor(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public InetSocketAddress address() {
		return new InetSocketAddress(host, port);
	}

	@Override
	public int compareTo(Anchor other) {
		if (other == this)
			return 0;

		int ret = host.compareTo(other.host);
		return (ret != 0) ? ret : port - other.port;
	}

	public boolean equals(Object object) {
		if (object instanceof Anchor) {
			Anchor other = (Anchor) object;
			return compareTo(other) == 0;
		} else {
			return false;
		}
	}
}
