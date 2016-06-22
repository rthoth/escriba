package test.id;

import io.escriba.store.IdGenerator;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class IdTest {

	IdGenerator idGen3 = new IdGenerator();
	IdGenerator idGen5 = new IdGenerator((short) 5);

	@Test
	public void t03() {
		String k0a = idGen3.generate("");
		String k1a = idGen3.generate("a");
		String k2a = idGen3.generate("aa");
		String k3a = idGen3.generate("aaa");
		String k4a = idGen3.generate("aaaa");
		String k5a = idGen3.generate("aaaaa");
		String k6a = idGen3.generate("aaaaaa");
	}

	@Test
	public void t05() {
		ArrayList<Object> list = new ArrayList<>(10);
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i <= 10; i++) {
			list.add(idGen5.generate(sb.toString()));
			sb.append('z');
		}
	}
}
