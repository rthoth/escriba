package io.escriba.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * PutHandler starts process of receive data from client
 */
public class PutHandler extends ChannelInboundHandlerAdapter {

	private final Config config;

	public PutHandler(Config config) {
		this.config = config;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Request) {
			PutHandlerStream processor = new PutHandlerStream((Request) msg, config);

			ctx.pipeline()
				.addAfter(ctx.name(), "putWorker", processor)
				.remove(ctx.name())
			;
		} else {
			ctx.fireChannelRead(msg);
		}
	}
}