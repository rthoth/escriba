package test;

import io.escriba.server.Config;
import io.escriba.Func;
import io.escriba.functional.F1;
import io.escriba.server.Method;
import io.escriba.server.Server;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.Socket;

public class DaemonStartupTest {

	@Test
	public void simple() throws Exception {
		Server daemon = new Server(getConfig(), 8081);
		daemon.start();

		Net.on("localhost", 8081, Func.t3(Method.PUT, "teste1", "key")).execute(new F1<Void, Socket>() {
			@Override
			public Void apply(Socket socket) throws IOException, InterruptedException {
				socket.getOutputStream().write("Teste".getBytes());
				socket.getOutputStream().flush();
				socket.close();
				Thread.sleep(10 * 10 * 1000);
				return null;
			}
		});
	}

	public Config getConfig() {
		return new Config(2, 2, null);
	}
}
