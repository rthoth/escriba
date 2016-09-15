package io.escriba.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DeleteHandler extends ChannelInboundHandlerAdapter {
	private ChannelHandlerContext ctx;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Request)
			delete((Request) msg);
		else
			ctx.fireChannelRead(msg);
	}

	private void delete(Request request) throws Exception {
		request.collection().remove(request.key)
			.complete(dataEntry -> {
				Http.responseAndClose(ctx, Http.okWithStatus(request.collectionName + "/" + request.key));
			})
			.error(throwable -> {
				ctx.fireExceptionCaught(throwable);
			})
			.start()
		;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}
}
