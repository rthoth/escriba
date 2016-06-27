package test;

import java.io.File;
import java.util.Date;

import static java.lang.String.format;

public class Help {

	private static File base = new File(format("build%stmp-test", File.separator));

	public static File newDir(String name, Class<?> clazz) {
		File file = new File(base, format("%s%s%s.%x", clazz.getSimpleName(), File.separator, name, new Date().getTime()));
		file.mkdirs();
		return file;
	}

	public static File newFile(String name, Class<?> clazz) {
		File file = new File(base, format("%s%s%s.%x", clazz.getSimpleName(), File.separator, name, new Date().getTime()));
		file.getParentFile().mkdirs();
		return file;
	}
}
