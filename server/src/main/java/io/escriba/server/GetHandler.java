package io.escriba.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.nio.channels.FileChannel;

import static io.netty.channel.ChannelFutureListener.CLOSE;

public class GetHandler extends ChannelInboundHandlerAdapter {

	private FileChannel channel = null;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		EscribaRequest request = (EscribaRequest) msg;
		channel = request.collection().getChannel(request.key);

		if (channel.size() > 0) {
			Http.response(ctx, Http.chunked(Http.ok("application/octet-stream")));

			ctx.pipeline().addBefore("processor", "chuncked", new ChunkedWriteHandler());
			ctx.writeAndFlush(new HttpChunkedInput(new ChunkedNioStream(channel, 500 * 1024)))
				.addListener(future -> {
					channel.close();
				})
				.addListener(CLOSE);
		} else {
			Http.responseAndClose(ctx, Http.noContent(request.collectionName + "/" + request.key));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (channel != null && channel.isOpen())
			channel.close();

		super.exceptionCaught(ctx, cause);
	}
}
