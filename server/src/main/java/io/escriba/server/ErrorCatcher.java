package io.escriba.server;

import io.escriba.EscribaException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class ErrorCatcher extends ChannelInboundHandlerAdapter {
	private DefaultFullHttpResponse createResponse(HttpResponseStatus status) {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
	}

	private static DefaultFullHttpResponse createResponse(HttpResponseStatus status, String message) {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(status.code(), message));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		HttpObject response = null;

		if (cause instanceof EscribaException.NoValue)
			response = noValue(ctx, (EscribaException.NoValue) cause);
		else
			response = internalError(ctx, cause);

		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private DefaultFullHttpResponse internalError(ChannelHandlerContext ctx, Throwable throwable) {
		DefaultFullHttpResponse response = createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());

		StringBuilder sb = new StringBuilder();

		do {
			sb.append(throwable.getClass().getName()).append("(").append(throwable.getMessage()).append(")");
			throwable = throwable.getCause();

			if (throwable != null)
				sb.append("->");

		} while (throwable != null);

		response.headers().add("X-Exception-trace", sb.toString());
		return response;
	}

	private DefaultFullHttpResponse noValue(ChannelHandlerContext ctx, EscribaException.NoValue noValueExc) {
		DefaultFullHttpResponse response = createResponse(HttpResponseStatus.NOT_FOUND, noValueExc.getMessage());
		return response;
	}
}
