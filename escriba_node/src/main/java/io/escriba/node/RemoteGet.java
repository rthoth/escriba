package io.escriba.node;

import io.escriba.DataEntry;
import io.escriba.EscribaException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static java.lang.Math.min;

public class RemoteGet<T> extends Get<T> {

	private int nextBytes;

	public RemoteGet(Bootstrap bootstrap, Postcard postcard, String key, int initialSize, PostcardReader<T> reader) throws InterruptedException {
		super(postcard, key, initialSize, reader);
		nextBytes = initialSize;

		bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
			@Override
			protected void initChannel(NioSocketChannel channel) throws Exception {
				channel.pipeline()
					.addLast(new HttpRequestEncoder())
					.addLast(new HttpResponseDecoder())
					.addLast(new Response());
			}
		}).connect(postcard.anchor.address()).addListener(future -> {
			ChannelFuture channelFuture = (ChannelFuture) future;

			if (channelFuture.isSuccess())
				channelFuture.channel().writeAndFlush(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/" + postcard.collection + "/" + key)).addListener(f -> {
					System.out.print(f);
				});
			else {
				completable.completeExceptionally(channelFuture.cause());
			}
		});

	}

	private class Response extends ChannelInboundHandlerAdapter {

		private CompositeByteBuf composite;
		private ChannelHandlerContext ctx;
		private DataEntry entry;
		private boolean inError = false;
		private HttpResponseStatus status;
		private long total = 0;

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof HttpResponse)
				channelRead((HttpResponse) msg);
			else if (inError) {
				composite.addComponent(true, ((HttpContent) msg).content());

				if (msg instanceof LastHttpContent) {
					String message = composite.readCharSequence(composite.readableBytes(), Charset.forName("UTF-8")).toString();

					EscribaException exception;
					if (status.equals(HttpResponseStatus.NOT_FOUND))
						exception = new EscribaException.NotFound(postcard.collection + "/" + key);

					else if (status.equals(HttpResponseStatus.BAD_REQUEST))
						exception = new EscribaException.IllegalArgument(message);

					else if (status.equals(HttpResponseStatus.NO_CONTENT))
						exception = new EscribaException.NoValue(postcard.collection + "/" + key);

					else
						exception = new EscribaException.Unexpected(message);

					completable.completeExceptionally(exception);

					ctx.close();
				}
			} else if (msg instanceof HttpContent)
				read((HttpContent) msg);
			else {
				// TODO:
			}
		}

		private void channelRead(HttpResponse response) {
			status = response.status();

			if (status.equals(HttpResponseStatus.OK)) {
				entry = DataEntry.DEFAULT.copy()
					.mediaType(response.headers().getAsString(HttpHeaderNames.CONTENT_TYPE))
					.size(response.headers().getInt(HttpHeaderNames.CONTENT_LENGTH).longValue())
					.end();
			} else {
				inError = true;
			}

		}

		@Override
		public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
			this.ctx = ctx;
			composite = ctx.alloc().compositeBuffer();
		}

		private void read(HttpContent response) {
			composite.addComponent(true, response.content());

			if (response instanceof LastHttpContent) {
				ByteBuffer buffer = composite.nioBuffer(0, composite.readableBytes());
				total += buffer.limit();

				Action<T> action = reader.apply(total, entry, buffer);
				composite.readerIndex(composite.readableBytes()).discardReadBytes();

				if (action instanceof Action.Stop)
					completable.complete(((Action.Stop<T>) action).value);
				else
					completable.completeExceptionally(new EscribaException.IllegalState("No object"));

				ctx.close();
			} else {
				while (composite.readableBytes() >= nextBytes) {

					ByteBuffer buffer = composite.nioBuffer(0, min(nextBytes, composite.readableBytes()));
					total += buffer.limit();

					composite.readerIndex(buffer.limit()).discardReadBytes();

					Action<T> action;
					try {
						action = reader.apply(total, entry, buffer);
					} catch (Exception exception) {
						ctx.close();
						completable.completeExceptionally(exception);
						return;
					}

					if (action instanceof Action.Read) {
						nextBytes = ((Action.Read) action).bytes;
					} else {
						ctx.close();
						completable.complete(((Action.Stop<T>) action).value);
						break;
					}
				}
			}
		}
	}
}
