package io.escriba.server;

import io.escriba.Store;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

public class Server {

	private final ServerBootstrap bootstrap;
	private final Config config;
	private final EventLoopGroup dispatchGroup;
	private final Store store;
	private final NioEventLoopGroup workGroup;

	public Server(Config config, Store store) {
		this.config = config;
		this.store = store;

		this.bootstrap = new ServerBootstrap();
		this.dispatchGroup = new NioEventLoopGroup(config.dispatchers);
		this.workGroup = new NioEventLoopGroup(config.workers);

		this.bootstrap.group(this.dispatchGroup, this.workGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new Server.Initializer(this))
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			.childOption(ChannelOption.WRITE_SPIN_COUNT, 1)
		;
	}

	public void listen(InetSocketAddress address) {
		new Thread(() -> {

			ChannelFuture future = null;
			try {
				// Start bind here!
				future = this.bootstrap.bind(address).sync();
			} catch (InterruptedException e) {
				// TODO: What to do?
				e.printStackTrace();
			}

			if (future != null) {
				try {
					// Wait here!
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
				.addLast("httpDecoder", new HttpRequestDecoder())
				.addLast("httpEncoder", new HttpResponseEncoder())
				.addLast("router", new Router(this.server.config, this.server.store))
				.addLast("errorCatcher", new ErrorCatcher())
			;
		}
	}
}
