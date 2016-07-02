package test;

import io.escriba.Store;
import io.escriba.server.Config;
import io.escriba.server.Server;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

@SuppressWarnings("unused")
public class ExternalTest implements Tester {
	private static final int timeout = 60 * 60 * 1000;

	@Test(timeOut = timeout)
	public void start() throws InterruptedException {
		Store store = new Store(newFile("mapdb"), newDir("data"), 8);
		Config config = new Config(2, 6);
		Server server = new Server(config, store);

		server.listen(new InetSocketAddress(12346));
		Thread.sleep(timeout);
	}
}