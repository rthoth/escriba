package test;

import io.escriba.Func;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static io.netty.util.CharsetUtil.UTF_8;

public class Net {

	public static void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {

		}
	}

	public static Connection on(String host, int port, Func.T3<Short, String, String> t3) throws IOException {
		return new Connection(host, port, t3);
	}

	public static class Connection {

		private final String host;
		private final int port;
		private final Func.T3<Short, String, String> t3;
		private Socket socket;

		private Connection(String host, int port, Func.T3<Short, String, String> t3) throws IOException {
			//this.socket = new Socket(InetAddress.getByName(host), port);
			this.host = host;
			this.port = port;
			this.t3 = t3;
			this.socket = new Socket();
			connect();
		}

		private void connect() {
			for (int i = 0; i < 10; i++) {

				try {
					socket.connect(new InetSocketAddress(host, port));
				} catch (ConnectException e) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
					}
					close(socket);
					socket = new Socket();
					continue;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				break;
			}
		}

		public <R> R execute(Func.F1<R, Socket> func) throws Exception {
			R r;

			try {
				OutputStream output = socket.getOutputStream();
				DataOutputStream dataOutput = new DataOutputStream(output);

				// method
				dataOutput.writeShort(t3.a.shortValue());

				// Collection
				byte[] bytes = t3.b.getBytes(UTF_8);
				dataOutput.writeShort(bytes.length);
				dataOutput.write(bytes);

				// Key
				bytes = t3.c.getBytes(UTF_8);
				dataOutput.writeShort(bytes.length);
				dataOutput.write(bytes);

				dataOutput.flush();

				r = func.apply(socket);
			} finally {
				Net.close(socket);
			}

			return r;
		}
	}
}
