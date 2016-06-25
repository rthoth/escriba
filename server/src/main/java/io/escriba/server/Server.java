package io.escriba.server.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class Server {

	private final InetSocketAddress address;
	private final ServerBootstrap bootstrap = new ServerBootstrap();
	private final NioEventLoopGroup clientGroup;
	private final Config config;
	private final NioEventLoopGroup dispatcherGroup;

	public Server(final Config config, final int port) {
		this(config, new InetSocketAddress(port));
	}

	public Server(final Config config, InetSocketAddress address) {
		dispatcherGroup = new NioEventLoopGroup(config.dispatcherPool);
		clientGroup = new NioEventLoopGroup(config.clientPool);

		bootstrap.group(dispatcherGroup, clientGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new Initializer(this))
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);

		this.config = config;
		this.address = address;
	}

	public void start() {
		new Thread(new MainThread(), "Escriba-main").start();
	}

	private static class Initializer extends ChannelInitializer<SocketChannel> {

		private final Server server;

		public Initializer(Server server) {
			this.server = server;
		}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new CommandHandler());
			ch.pipeline().addLast(new StoreHandler(server.config.store));
		}
	}

	private class MainThread implements Runnable {
		@Override
		public void run() {
			try {
				bootstrap.bind(address).sync();
			} catch (InterruptedException e) {
				dispatcherGroup.shutdownGracefully();
				clientGroup.shutdownGracefully();
			}
		}
	}
}
