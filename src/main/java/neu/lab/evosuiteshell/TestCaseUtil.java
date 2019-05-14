package neu.lab.evosuiteshell;

import java.io.File;

public class TestCaseUtil {
	public static boolean removeFileDir(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				removeFileDir(f);
			}
		}
		return file.delete();
	}
}
