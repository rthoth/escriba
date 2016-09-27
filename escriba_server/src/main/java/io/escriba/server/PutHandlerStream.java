package io.escriba.server;

import io.escriba.DataEntry;
import io.escriba.Putter;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.ByteBuffer;

public class PutHandlerStream extends ChannelInboundHandlerAdapter {

	private ByteBuffer cache;

	private CompositeByteBuf compositeBuffer;

	private final Config config;

	private Putter.Control control;

	private ChannelHandlerContext ctx;

	private final LockedBlock lockedBlock = new LockedBlock();

	private final Request request;

	private boolean shouldClose;

	private boolean shouldWrite;

	private boolean writing;


	public PutHandlerStream(Request request, Config config) throws Exception {
		this.request = request;
		this.config = config;

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
		compositeBuffer = ctx.alloc().compositeDirectBuffer(config.putMaxFrames);
		cache = ByteBuffer.allocate(config.putCacheSize);
		this.ctx = ctx;
	}

	private void onError(Throwable throwable) throws Exception {
		lockedBlock.locked(throwable::printStackTrace);
	}

	private void onReady(Putter.Control control) throws Exception {
		lockedBlock.locked(() -> {
			this.control = control;
			shouldWrite = true;
			writeOrClose();
		});
	}

	private void onWritten(int written, ByteBuffer buffer, Putter.Control control) throws Exception {
		lockedBlock.locked(() -> {
			compositeBuffer.skipBytes(written).discardReadBytes();
			shouldWrite = true;
			writing = false;
			writeOrClose();
		});
	}

	private void writeOrClose() throws Exception {

		if (control == null || compositeBuffer == null)
			return;

		if (shouldWrite && compositeBuffer.readableBytes() >= config.putCacheSize) {
			shouldWrite = false;
			writing = true;
			cache.clear();

			compositeBuffer.markReaderIndex();
			compositeBuffer.readBytes(cache);
			compositeBuffer.resetReaderIndex();

			cache.flip();
//			write.apply(cache);
			control.write(cache);
			return;
		}

		if (shouldClose && !writing) {

			if (compositeBuffer.readableBytes() > 0) {
				cache.clear().limit(Math.min(compositeBuffer.readableBytes(), config.putCacheSize));

				compositeBuffer.markReaderIndex();
				compositeBuffer.readBytes(cache);
				compositeBuffer.resetReaderIndex();

				cache.flip();
//				write.apply(cache);
				control.write(cache);
			} else {
				try {
//					close.apply();
					control.close();
				} catch (Exception e) {
					// TODO: What to do?
				} finally {
					compositeBuffer.release();
					Http.responseAndClose(ctx, Http.created(request.collectionName + "/" + request.key));
				}
			}
		}

	}
}