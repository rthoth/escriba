package io.escriba.server;

import io.escriba.DataChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.handler.stream.ChunkedWriteHandler;

import static io.netty.channel.ChannelFutureListener.CLOSE;

public class GetHandler extends ChannelInboundHandlerAdapter {

	private DataChannel channel;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Request request = (Request) msg;
		channel = request.collection().getChannel(request.key);

		if (channel != null) {
			if (channel.size() > 0) {
				Http.response(ctx, Http.chunked(Http.ok("application/octet-stream")));

				ctx.pipeline().addBefore("processor", "chuncked", new ChunkedWriteHandler());
				ctx.writeAndFlush(new HttpChunkedInput(new ChunkedNioStream(this.channel, 500 * 1024)))
					.addListener(future -> {
						channel.close();
					})
					.addListener(CLOSE);
			} else {
				Http.responseAndClose(ctx, Http.noContent(request.collectionName + "/" + request.key));
			}
		} else {
			Http.responseAndClose(ctx, Http.notFound(request.collectionName + "/" + request.key));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (channel != null && channel.isOpen())
			channel.close();

		super.exceptionCaught(ctx, cause);
	}
}
