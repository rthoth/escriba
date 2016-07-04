package io.escriba.server;

import io.escriba.EscribaException;
import io.escriba.EscribaException.NoValue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
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
		HttpResponse response;

		if (cause instanceof NoValue)
			response = noValue((NoValue) cause);

		else if (cause instanceof EscribaException.NotFound)
			response = notFound((EscribaException.NotFound) cause);

		else
			response = internalError(cause);

		Http.responseAndClose(ctx, response);
	}

	private DefaultFullHttpResponse internalError(Throwable throwable) {
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

	private DefaultFullHttpResponse noValue(NoValue noValueExc) {
		DefaultFullHttpResponse response = ErrorCatcher.createResponse(HttpResponseStatus.NOT_FOUND, noValueExc.getMessage());
		return response;
	}

	private HttpResponse notFound(EscribaException.NotFound cause) {
		return Http.notFound(cause.getMessage());
	}
}
