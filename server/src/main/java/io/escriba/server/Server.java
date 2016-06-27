package io.escriba.server;

import io.escriba.Store;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;

import java.net.InetSocketAddress;

public class Server {

	private final ServerBootstrap bootstrap;
	private final Config config;
	private final io.netty.channel.EventLoopGroup dispatchGroup;
	private final Store store;
	private final NioEventLoopGroup workGroup;

	public Server(Config config, Store store) {
		this.config = config;
		this.store = store;

		bootstrap = new ServerBootstrap();
		dispatchGroup = new NioEventLoopGroup(config.dispatchers);
		workGroup = new NioEventLoopGroup(config.workers);

		bootstrap.group(dispatchGroup, workGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new Initializer(this));
	}

	public void listen(InetSocketAddress address) {
		new Thread(() -> {

			ChannelFuture future = null;
			try {
				future = bootstrap.bind(address).sync();
			} catch (InterruptedException e) {
				// TODO: What to do?
				e.printStackTrace();
			}

			if (future != null) {
				try {
					future.channel().closeFuture().sync();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}, "Escriba-main").start();
	}

	private static class Initializer extends ChannelInitializer<NioSocketChannel> {
		private final Server server;

		public Initializer(Server server) {
			this.server = server;
		}

		@Override
		protected void initChannel(NioSocketChannel ch) throws Exception {
			ch.pipeline()
				.addLast(new HttpRequestDecoder())
				.addLast(new Router(server.config, server.store));
		}
	}
}
