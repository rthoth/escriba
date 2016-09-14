package test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

public interface HttpTest {

	default void get(String url, HttpTest.ResponseHandler handler) throws Exception {
		handler.apply(Unirest.get(format("http://%s", url)).asBinary());
	}

	default void put(String url, byte[] bytes, HttpTest.ResponseHandler handler) throws Exception {
		handler.apply(Unirest.put(format("http://%s", url)).body(bytes).asBinary());
	}

	interface ResponseHandler {
		void apply(HttpResponse<InputStream> response) throws IOException;
	}
}
