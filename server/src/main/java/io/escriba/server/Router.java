package io.escriba.server;

import io.escriba.Store;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router extends ChannelInboundHandlerAdapter {

	private static final Pattern PATTERN = Pattern.compile("^/([0-9a-z]+)/([0-9a-z]+)$", Pattern.CASE_INSENSITIVE);

	private final Config config;
	private final Store store;

	public Router(Config config, Store store) {
		this.config = config;
		this.store = store;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest)
			route((HttpRequest) msg, ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().closeFuture().addListener(future -> {
			System.out.println("Essa conexão foi fechada por causa disso!");
			cause.printStackTrace();
		});
	}

	private void route(HttpRequest request, ChannelHandlerContext ctx) {
		Matcher matcher = Router.PATTERN.matcher(request.uri());

		if (matcher.find()) {
			String collection = matcher.group(1);
			String key = matcher.group(2);

			ChannelInboundHandler handler = null;

			if (request.method() == HttpMethod.PUT)
				handler = new PutHandler();

			else if (request.method() == HttpMethod.GET)
				handler = new GetHandler();

			if (handler != null) {

				ctx.pipeline()
					.addAfter("router", "processor", handler)
					.remove("router")
				;

				ctx.fireChannelRead(new Request(this.config, this.store, collection, key, request));
			} else {
				// TODO: Exception?
			}
		} else {
			// TODO: Exception?
		}
	}
}
