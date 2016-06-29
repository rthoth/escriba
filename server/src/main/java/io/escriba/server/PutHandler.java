package io.escriba.server;

import io.escriba.DataEntry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;

public class PutHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		EscribaRequest request = (EscribaRequest) msg;

		FullHttpRequest httpRequest = request.httpRequest;
		ByteBuf content = httpRequest.content();

		String mediaType = httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
		if (mediaType == null)
			mediaType = DataEntry.DEFAULT_MEDIA_TYPE;

		request.collection().put(request.key, mediaType)
			.ready((write, close) -> {
				write.apply(content.nioBuffer(), 0L);
			})
			.written((total, last, write, close) -> {
				if (total < content.readableBytes())
					write.apply(content.nioBuffer((int) total, (int) (content.readableBytes() - total)), total);
				else {
					close.apply();
					Http.responseAndClose(ctx, Http.created(request.collectionName + "/" + request.key));
				}
			})
			.error(throwable -> {
				// TODO:
			})
			.async();
	}
}
