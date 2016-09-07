package io.escriba.server;

import io.escriba.Getter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class GetHandler extends ChannelInboundHandlerAdapter {

	private static final int CHUNK_SIZE = 1024 * 512;
	private ChannelHandlerContext ctx;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Request) {
			get((Request) msg);
		} else
			ctx.fireChannelRead(msg);
	}

	private void get(Request request) throws Exception {
		new GetChunkedResponse(request.get(), ctx, CHUNK_SIZE);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}

	private static class GetChunkedResponse {
		private final int chunkSize;
		private final ChannelHandlerContext ctx;

		public GetChunkedResponse(Getter getter, ChannelHandlerContext ctx, int chunkSize) {
			this.ctx = ctx;
			this.chunkSize = chunkSize;

//			getter
//				.ready(this::onReady)
//				.read(this::onRead)
//				.error(this::onError)
//				.start()
//			;
		}

	}
}
