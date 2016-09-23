package io.escriba.node;

import io.escriba.EscribaException;
import io.escriba.ProxyFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.Future;

import static java.lang.Math.min;

public class RemotePut<T> extends Put<T> {

	private static final int MIN_BUFFER = 128 * 1024;

	public RemotePut(Bootstrap bootstrap, Postcard postcard, String key, String mediaType, T content, PostcardWriter<T> writer) {
		super(postcard, key, mediaType, content, writer);

		bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
			@Override
			protected void initChannel(NioSocketChannel channel) throws Exception {
				channel.pipeline()
					.addLast(new HttpRequestEncoder())
					.addLast(new HttpResponseDecoder())
					.addLast(new Request())
					.addLast(new HttpObjectAggregator(8 * 1024))
					.addLast(new Response());
			}
		}).connect(postcard.anchor.address()).addListener(future -> {
			ChannelFuture channelFuture = (ChannelFuture) future;

			if (channelFuture.isSuccess())
				channelFuture.channel().pipeline().get(Request.class).start();
			else
				completable.completeExceptionally(channelFuture.cause());
		});
	}

	private static String contentOf(FullHttpResponse response) {
		return response.content().readCharSequence(response.content().readableBytes(), Charset.forName("UTF-8")).toString();
	}

	@Override
	public Future<Postcard> future() {
		return new ProxyFuture<>(completable);
	}

	public class Request extends ChannelOutboundHandlerAdapter {

		private CompositeByteBuf composite;
		private ChannelHandlerContext ctx;
		private boolean hasMore = true;
		private GenericFutureListener<ChannelFuture> listener = future -> serialize();

		@Override
		public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
			this.ctx = ctx;
			composite = ctx.alloc().compositeBuffer();
		}

		private void serialize() throws Exception {

			if (hasMore) {
				ByteBuffer buffer;

				while (composite.readableBytes() < MIN_BUFFER) {

					buffer = writer.apply(content);
					if (buffer != null)
						composite.addComponent(true, ctx.alloc().heapBuffer(buffer.limit(), buffer.limit()).writeBytes(buffer));
					else {
						hasMore = false;
						break;
					}
				}
			}

			if (composite.isReadable()) {
				ByteBuf buf = composite.copy(0, min(MIN_BUFFER, composite.readableBytes()));
				composite.readerIndex(buf.readableBytes()).discardReadBytes();

				HttpContent httpContent = (!composite.isReadable() && !hasMore) ?
					new DefaultLastHttpContent(buf) :
					new DefaultHttpContent(buf);

				ctx.writeAndFlush(httpContent).addListener(listener);
			}
		}

		public void start() throws Exception {

			DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/" + postcard.collection + "/" + key);

			request.headers()
				.add(HttpHeaderNames.CONTENT_TYPE, mediaType)
				.add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

			ctx.writeAndFlush(request).addListener(listener);
		}
	}

	public class Response extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof FullHttpResponse)
				onResponse(ctx, (FullHttpResponse) msg);
			else
				ctx.close();
		}

		private void onResponse(ChannelHandlerContext ctx, FullHttpResponse response) {
			if (response.status().equals(HttpResponseStatus.CREATED)) {
				completable.complete(postcard);
			} else {
				HttpResponseStatus status = response.status();

				Exception exception;

				if (status.equals(HttpResponseStatus.NOT_FOUND))
					exception = new EscribaException.NotFound(postcard.collection + "/" + key);
				else if (status.equals(HttpResponseStatus.BAD_REQUEST))
					exception = new EscribaException.IllegalArgument(contentOf(response));
				else if (status.equals(HttpResponseStatus.NO_CONTENT))
					exception = new EscribaException.NoValue(postcard.collection + "/" + key);
				else
					exception = new EscribaException.Unexpected(contentOf(response));

				completable.completeExceptionally(exception);
			}

			ctx.close();
		}
	}
}
