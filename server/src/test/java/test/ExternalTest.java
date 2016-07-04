package test;

import io.escriba.DataDir;
import io.escriba.Store;
import io.escriba.T2;
import io.escriba.server.Config;
import io.escriba.server.Server;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

@SuppressWarnings("unused")
public class ExternalTest implements Tester {
	private static final int timeout = 60 * 60 * 1000;

	@Test(timeOut = timeout)
	public void start() throws InterruptedException {
		Store store = new Store(newFile("mapdb"), DataDir.of(T2.of(2, newDir("data-1-2")), T2.of(4, newDir("data-2-4")), T2.of(2, newDir("data-3-3"))), 8);
		Config config = new Config(2, 6);
		Server server = new Server(config, store);

		server.listen(new InetSocketAddress(12346));
		Thread.sleep(timeout);
	}
}