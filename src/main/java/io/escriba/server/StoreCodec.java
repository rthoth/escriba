package io.escriba.server;


import io.escriba.Func;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import static io.netty.util.CharsetUtil.UTF_8;

public class StoreCodec extends ChannelInboundHandlerAdapter {

	public static Func.T2<String, String> readCollectionAndKey(ChannelHandlerContext ctx, ByteBuf buffer) {
		byte[] buf = new byte[buffer.readShort()];
		buffer.readBytes(buf);

		String name = new String(buf, UTF_8);

		buf = new byte[buffer.readShort()];

		buffer.readBytes(buf);
		String key = new String(buf, UTF_8);

		return Func.t2(name, key);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof ByteBuf)
			parse(ctx, (ByteBuf) msg);

		ctx.fireChannelRead(null);
	}

	private void parse(ChannelHandlerContext ctx, ByteBuf buffer) {
		short method = buffer.readShort();
		switch (method) {
			case Method.GET:
				get(ctx, buffer);
				break;
			case Method.PUT:
				put(ctx, buffer);
		}
	}

	private void put(ChannelHandlerContext ctx, ByteBuf buffer) {
		Func.T2<String, String> t2 = readCollectionAndKey(ctx, buffer);
		ctx.fireChannelRead(Command.put(t2));
	}

	private void get(ChannelHandlerContext ctx, ByteBuf buffer) {
		Func.T2<String, String> t2 = readCollectionAndKey(ctx, buffer);
		ctx.fireChannelRead(Command.get(t2));
	}
}
