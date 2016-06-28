package io.escriba.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import static java.lang.String.format;

public class PutHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		EscribaRequest request = (EscribaRequest) msg;

		FullHttpRequest httpRequest = request.httpRequest;
		ByteBuf content = httpRequest.content();
		final int length = httpRequest.content().readableBytes();
		final long[] pos = {0L};

		request.collection().put(request.key,
			(write, close) -> {
				write.apply(content.nioBuffer(), pos[0]);
			},
			(total, buffer, write, close) -> {
				pos[0] += total;
				if (pos[0] < length) {
					write.apply(content.nioBuffer(), pos[0]);
				} else {
					close.apply();
					Http.responseAndClose(ctx, Http.created(format("%s/%s", request.collectionName, request.key)));
				}
			},
			throwable -> {
				ctx.fireExceptionCaught(throwable);
			});
	}
}
