package io.escriba.server;

import io.escriba.Close;
import io.escriba.DataEntry;
import io.escriba.Getter;
import io.escriba.Read;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.ByteBuffer;

import static io.netty.channel.ChannelFutureListener.CLOSE;

public class GetHandler extends ChannelInboundHandlerAdapter {

	private static final int CHUNK_SIZE = 1024 * 512;
	private ChannelHandlerContext ctx;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Request) {
			process((Request) msg);
		} else
			ctx.fireChannelRead(msg);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}

	private void process(Request request) throws Exception {
		new GetChunkedResponse(request.get(), ctx, CHUNK_SIZE);
	}

	private static class GetChunkedResponse {
		private final int chunkSize;
		private final ChannelHandlerContext ctx;

		public GetChunkedResponse(Getter getter, ChannelHandlerContext ctx, int chunkSize) {
			this.ctx = ctx;
			this.chunkSize = chunkSize;

			getter
				.ready(this::onReady)
				.read(this::onRead)
				.error(this::onError)
				.start()
			;
		}

		private void onError(Throwable throwable) {
			// TODO: Log?
			throwable.printStackTrace();
			ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(CLOSE);
		}

		private void onRead(int bytes, ByteBuffer buffer, Read read, Close close) throws Exception {
			if (bytes >= 0) {

				if (bytes > 0) {
					buffer.limit(bytes).rewind();
					ByteBuf buf = ctx.alloc().buffer(bytes);
					buf.writeBytes(buffer);

					ctx.writeAndFlush(buf);
				}

				buffer.clear();
				read.apply(buffer);
			} else {
				try {
					close.apply();
				} finally {
					ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(CLOSE);
				}
			}
		}

		private void onReady(DataEntry entry, Read read, Close close) throws Exception {
			HttpResponse httpResponse = Http.chunked(Http.ok(entry.mediaType));
			httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, entry.size);
			ctx.writeAndFlush(httpResponse);
			read.apply(ByteBuffer.allocateDirect(chunkSize));
		}
	}
}
