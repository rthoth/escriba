package io.escriba.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * PutHandler starts process of receive data from client
 */
public class PutHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Request) {
			PutHandlerWorker processor = new PutHandlerWorker((Request) msg);

			ctx.pipeline()
				.addAfter(ctx.name(), "putWorker", processor)
				.remove(ctx.name())
			;
		} else {
			ctx.fireChannelRead(msg);
		}
	}
}