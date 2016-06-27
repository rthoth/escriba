package test;

import io.escriba.Store;
import io.escriba.server.Config;
import io.escriba.server.Server;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

public class GetTest implements Tester, HttpTest {

	private final Config config = new Config(2, 2);
	private final Server server;
	private final Store store = new Store(newFile("mapdb"), newDir("datadir"));

	public GetTest() throws InterruptedException {
		server = new Server(config, store);
		server.listen(new InetSocketAddress("localhost", 12345));
		Thread.sleep(500);
	}


	@Test()
	public void t01() throws IOException, InterruptedException {
		get("localhost:12345/col/val", (connection) -> {
			connection.getInputStream();
		});
	}
}
