package io.escriba.server;

import io.escriba.Store;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

@SuppressWarnings("unused")
public class Server {

	private final ServerBootstrap bootstrap;
	private final Config config;
	private InetSocketAddress listenAddress;
	private final Store store;

	public Server(Config config, Store store) {
		this.config = config;
		this.store = store;

		bootstrap = new ServerBootstrap();
		EventLoopGroup dispatchers = new NioEventLoopGroup(config.dispatchers);
		EventLoopGroup workers = new NioEventLoopGroup(config.workers);

		bootstrap.group(dispatchers, workers)
			.channel(NioServerSocketChannel.class)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			.childOption(ChannelOption.WRITE_SPIN_COUNT, 1)
			.childHandler(new ChannelInitializer<NioServerSocketChannel>() {
				@Override
				protected void initChannel(NioServerSocketChannel channel) throws Exception {
					channel.pipeline()
						.addLast("httpDecoder", new HttpRequestDecoder())
						.addLast("httpEncoder", new HttpResponseEncoder())
						.addLast("router", new Router(config, store))
						.addLast("errorCatcher", new ErrorCatcher());
				}
			})
		;
	}

	@SuppressWarnings("unused")
	public void listen(InetSocketAddress address) {
		listenAddress = address;
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

	public InetSocketAddress listenAddress() {
		return listenAddress;
	}

	public Store store() {
		return this.store;
	}
}
