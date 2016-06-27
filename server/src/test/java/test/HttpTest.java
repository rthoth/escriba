package test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public interface HttpTest {

	default void get(String url, RespondeHandler handler) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(String.format("http://%s", url)).openConnection();

		connection.setRequestMethod("GET");
		connection.connect();

		handler.apply(connection);
	}

	interface RespondeHandler {
		void apply(HttpURLConnection connection) throws IOException;
	}
}
