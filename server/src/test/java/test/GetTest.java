package test;

import io.escriba.Store;
import io.escriba.server.Config;
import io.escriba.server.Server;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

public class GetTest implements Tester, HttpTest {

	private final Config config = new Config(2, 2);
	private final Server server;
	private final Store store = new Store(this.newFile("mapdb"), this.newDir("datadir"));

	public GetTest() throws InterruptedException {
		this.server = new Server(this.config, this.store);
		this.server.listen(new InetSocketAddress("localhost", 12345));
		Thread.sleep(3000);
	}

	@Test(groups = "put")
	public void t00() throws Exception {
		this.put("localhost:12345/col/val", "Some data where!".getBytes(), response -> {
			assertThat(response.getStatus()).isEqualTo(201);
			assertThat(response.getStatusText()).isEqualTo("col/val");
		});
	}


	@Test(dependsOnGroups = "put")
	public void t01() throws Exception {
		this.get("localhost:12345/col/val", response -> {
			assertThat(response.getStatus()).isEqualTo(200);
			assertThat(IOUtils.toString(response.getBody(), "utf8")).isEqualTo("Some data where!");
		});
	}
}
