package io.escriba.server;

import io.escriba.EscribaException;
import io.escriba.EscribaException.NoValue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.Charset;

public class ErrorCatcher extends ChannelInboundHandlerAdapter {

	private static Charset charset() {
		return Charset.forName("UTF-8");
	}

	private static ByteBuf content(ChannelHandlerContext ctx, String content) {
		byte[] bytes = content.getBytes(charset());
		return ctx.alloc().buffer(bytes.length).writeBytes(bytes);
	}

	private static DefaultFullHttpResponse createResponse(HttpResponseStatus status, ByteBuf content) {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		HttpResponse response;

		if (cause instanceof NoValue)
			response = noValue(ctx, (NoValue) cause);
		else if (cause instanceof EscribaException.NotFound)
			response = notFound((EscribaException.NotFound) cause);
		else
			response = internalError(ctx, cause);

		Http.responseAndClose(ctx, response);
	}

	private DefaultFullHttpResponse internalError(ChannelHandlerContext ctx, Throwable throwable) {

		DefaultFullHttpResponse response = createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, content(ctx, throwable.getMessage()));
		StringBuilder sb = new StringBuilder();

		do {
			sb.append(throwable.getClass().getName()).append("(").append(throwable.getMessage()).append(")");
			throwable = throwable.getCause();

			if (throwable != null)
				sb.append("->");

		} while (throwable != null);

		response.content().writeCharSequence(sb.toString(), Charset.defaultCharset());
		return response;
	}

	private DefaultFullHttpResponse noValue(ChannelHandlerContext ctx, NoValue noValueExc) {
		DefaultFullHttpResponse response = ErrorCatcher.createResponse(HttpResponseStatus.NOT_FOUND, content(ctx, noValueExc.getMessage()));
		return response;
	}

	private HttpResponse notFound(EscribaException.NotFound cause) {
		return Http.notFound(cause.getMessage());
	}
}
