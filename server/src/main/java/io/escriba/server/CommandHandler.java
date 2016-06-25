package io.escriba.server.server;


import io.escriba.Func;
import io.escriba.functional.T2;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import static io.netty.util.CharsetUtil.UTF_8;

public class CommandHandler extends ChannelInboundHandlerAdapter {

	private static final byte GET_CODE = 20;
	private static final byte PUT_CODE = 10;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof ByteBuf)
			read(ctx, (ByteBuf) msg);
		else
			ctx.fireChannelRead(null);
	}

	private void get(ChannelHandlerContext ctx, ByteBuf buffer) {
		T2<String, String> t2 = readCollectionAndKey(ctx, buffer);
		ctx.fireChannelRead(Command.get(t2));
	}

	private void put(ChannelHandlerContext ctx, ByteBuf buffer) {
		T2<String, String> t2 = readCollectionAndKey(ctx, buffer);
		ctx.fireChannelRead(Command.put(t2));
	}

	private void read(ChannelHandlerContext ctx, ByteBuf buffer) {
		byte method = buffer.readByte();
		switch (method) {
			case GET_CODE:
				get(ctx, buffer);
				break;
			case PUT_CODE:
				put(ctx, buffer);
		}
	}

	public static T2<String, String> readCollectionAndKey(ChannelHandlerContext ctx, ByteBuf buffer) {
		byte[] buf = new byte[buffer.readShort()];
		buffer.readBytes(buf);

		String name = new String(buf, UTF_8);

		buf = new byte[buffer.readShort()];

		buffer.readBytes(buf);
		String key = new String(buf, UTF_8);

		return Func.t2(name, key);
	}
}
