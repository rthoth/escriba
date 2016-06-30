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

import static java.lang.Math.min;

public class PutHandlerWorker extends ChannelInboundHandlerAdapter {
	private static final int MAX_BYTES = 1024 * 64;
	private static final int MAX_FRAMES = 1024 * 512;
	private Close close;
	private CompositeByteBuf compositeBuffer;
	private ChannelHandlerContext ctx;
	private final LockedBlock lockedBlock = new LockedBlock();
	private final Request request;
	private boolean shouldClose;
	private boolean shouldWrite;
	private int total;
	private Write write;

	public PutHandlerWorker(Request request) throws Exception {
		this.request = request;

		String mediaType = request.httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
		if (mediaType == null)
			mediaType = DataEntry.DEFAULT_MEDIA_TYPE;

		request.collection().put(request.key, mediaType)
			.ready(this::onReady)
			.written(this::onWritten)
			.error(this::onError)
			.async()
		;
	}

	private void addContent(HttpContent httpContent) throws Exception {
		compositeBuffer.addComponent(true, httpContent.content());
		check();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (compositeBuffer == null) {
			compositeBuffer = ctx.alloc().compositeBuffer(MAX_FRAMES);
			this.ctx = ctx;
		}

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

	private void check() throws Exception {

		if (shouldWrite)
			if (compositeBuffer != null && compositeBuffer.readableBytes() > 0) {
				if (write != null) {
					shouldWrite = false;
					write.apply(compositeBuffer.nioBuffer(0, min(MAX_BYTES, compositeBuffer.readableBytes())), total);
					return;
				}
			}

		if (shouldClose && compositeBuffer != null && compositeBuffer.readableBytes() == 0) {
			if (close != null)
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

	private void onError(Throwable throwable) throws Exception {
		lockedBlock.locked(() -> {
			throwable.printStackTrace();
		});
	}

	private void onReady(Write write, Close close) throws Exception {
		lockedBlock.locked(() -> {
			this.write = write;
			this.close = close;
			shouldWrite = true;
			check();
		});
	}

	private void onWritten(long total, int last, Write write, Close close) throws Exception {
		lockedBlock.locked(() -> {
			compositeBuffer.skipBytes(last).discardReadBytes();
			this.total = (int) total;
			shouldWrite = true;
			check();
		});
	}
}