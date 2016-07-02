package test;

import java.io.File;
import java.util.Date;

public interface Tester {

	default File newDir(String name) {
		File file = new File(newTmp(), name + "." + Long.toString(new Date().getTime(), 32));
		file.mkdirs();
		return file;
	}

	default File newFile(String name) {
		return new File(newTmp(), name + "." + Long.toString(new Date().getTime(), 32));
	}

	default File newTmp() {
		File file = new File("build" + File.separator + getClass().getSimpleName());
		if (!file.exists())
			file.mkdirs();

		return file;
	}
}
