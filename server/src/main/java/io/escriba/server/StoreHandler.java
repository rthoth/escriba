package io.escriba.server.server;

import io.escriba.store.Store;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class StoreHandler extends ChannelInboundHandlerAdapter {

	private final Store store;

	public StoreHandler(Store store) {
		this.store = store;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Command.Put)
			put((Command.Put) msg);
		else if (msg instanceof Command.Get)
			get((Command.Get) msg);
	}

	private void get(Command.Get get) {

	}

	private void put(Command.Put put) {

	}

}
