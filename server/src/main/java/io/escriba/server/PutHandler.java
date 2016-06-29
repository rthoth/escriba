package io.escriba.server;

import io.escriba.Close;
import io.escriba.DataEntry;
import io.escriba.Write;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

public class PutHandler extends ChannelInboundHandlerAdapter {

	private ByteBuf buffer = null;
	private LockedBlock lockedBlock = new LockedBlock();
	private Request request = null;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Request) {

			request = (Request) msg;
			buffer = Unpooled.buffer();
			lockedBlock.locked(this::prepare);

		} else if (msg instanceof HttpContent) {
			if (!(msg instanceof LastHttpContent)) {
				// chunk

			} else {
				// end
			}
		}
	}

	private void onError(Throwable throwable) {

	}

	private void onWritten(long total, int last, Write write, Close close) {

	}

	private void prepare() throws Exception {

		HttpRequest httpRequest = request.httpRequest;

		String mediaType = httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
		if (mediaType == null)
			mediaType = DataEntry.DEFAULT_MEDIA_TYPE;

		request.collection().put(request.key, mediaType)
			.written((total, last, write, close) -> lockedBlock.locked(() -> onWritten(total, last, write, close)))
			.error(throwable -> lockedBlock.locked(() -> onError(throwable)))
			.async();
	}
}
