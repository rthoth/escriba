package io.escriba.node;

import io.escriba.Store;
import io.escriba.server.Server;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.Future;

@SuppressWarnings("unused")
public class Node {

	public final Anchor anchor;
	private final Bootstrap bootstrap;
	public final Server server;
	public final Store store;

	public Node(Server server, NodeConfig config) {
		this.server = server;
		this.anchor = new Anchor(server);
		this.store = server.store();

		bootstrap = new Bootstrap()
			.group(new NioEventLoopGroup(config.threads))
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true);
	}

	public Bootstrap bootstrap() {
		return bootstrap.clone();
	}

	public <T> Future<T> get(Postcard postcard, String key, int initialSize, PostcardReader<T> reader) throws Exception {
		return postcard.get(this, key, initialSize, reader);
	}

	public Postcard postcard(String collection) {
		return new Postcard(collection, server);
	}

	public <T, P> Future<Postcard> put(Postcard postcard, String key, String mediaType, P previous, T content, PostcardWriter<T, P> writer) throws Exception {
		return postcard.put(this, key, mediaType, previous, content, writer);
	}

	public static class NodeConfig {

		public final int threads;

		public NodeConfig(int threads) {
			this.threads = threads;
		}
	}
}
