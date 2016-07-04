package io.escriba.server;

import io.escriba.Close;
import io.escriba.DataEntry;
import io.escriba.Write;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.ByteBuffer;

import static java.lang.Math.min;

public class PutHandlerStream extends ChannelInboundHandlerAdapter {
	private static final int MAX_BYTES = 1024 * 64;
	private static final int MAX_FRAMES = 1024 * 512;
	private Close close;
	private CompositeByteBuf compositeBuffer;
	private ChannelHandlerContext ctx;
	private final LockedBlock lockedBlock = new LockedBlock();
	private final Request request;
	private boolean shouldClose;
	private boolean shouldWrite;
	private Write write;

	public PutHandlerStream(Request request) throws Exception {
		this.request = request;

		String mediaType = request.httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
		if (mediaType == null)
			mediaType = DataEntry.DEFAULT_MEDIA_TYPE;

		request.collection().put(request.key, mediaType)
			.ready(this::onReady)
			.written(this::onWritten)
			.error(this::onError)
			.start()
		;
	}

	private void addContent(HttpContent httpContent) throws Exception {
		compositeBuffer.addComponent(true, httpContent.content());
		writeOrClose();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpContent) {
			if (!(msg instanceof LastHttpContent)) {
				lockedBlock.locked(() -> addContent((HttpContent) msg));
			} else {
				lockedBlock.locked(() -> {
					shouldClose = true;
					addContent((LastHttpContent) msg);
				});
			}
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		compositeBuffer = ctx.alloc().compositeDirectBuffer(MAX_FRAMES);
		this.ctx = ctx;
	}

	private void onError(Throwable throwable) throws Exception {
		lockedBlock.locked(throwable::printStackTrace);
	}

	private void onReady(Write write, Close close) throws Exception {
		lockedBlock.locked(() -> {
			this.write = write;
			this.close = close;
			shouldWrite = true;
			writeOrClose();
		});
	}

	private void onWritten(int written, ByteBuffer buffer, Write write, Close close) throws Exception {
		lockedBlock.locked(() -> {
			compositeBuffer.skipBytes(written).discardReadBytes();
			shouldWrite = true;
			writeOrClose();
		});
	}

	private void writeOrClose() throws Exception {

		if (write == null)
			return;

		if (shouldWrite && compositeBuffer != null && compositeBuffer.readableBytes() > 0) {
			shouldWrite = false;
			write.apply(compositeBuffer.nioBuffer(0, min(MAX_BYTES, compositeBuffer.readableBytes())));
			return;
		}

		if (shouldClose && compositeBuffer != null && compositeBuffer.readableBytes() == 0) {
			try {
				close.apply();
			} catch (Exception e) {
				// TODO: What to do?
			} finally {
				compositeBuffer.release();
				Http.responseAndClose(ctx, Http.created(request.collectionName + "/" + request.key));
			}
		}

	}
}