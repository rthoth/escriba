package io.escriba.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class GetHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		EscribaRequest request = (EscribaRequest) msg;

		request.collection().get(request.key, (read, close) -> {

		}, (total, buffer, read, close) -> {

		}, (throwable) -> {

		});
	}
}
