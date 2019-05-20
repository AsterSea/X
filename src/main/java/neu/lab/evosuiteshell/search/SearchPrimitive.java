package neu.lab.evosuiteshell.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import neu.lab.evosuiteshell.TestCaseUtil;

public class SearchPrimitive {
	private static SearchPrimitive instance = new SearchPrimitive();

	private SearchPrimitive() {

	}

	public static SearchPrimitive getInstance() {
		return instance;
	}

	public void getValueFromJavaFile() {
		String dir = System.getProperty("user.dir") + "\\src";
		HashSet<String> filesPath = TestCaseUtil.getFiles(dir);
		for (String path : filesPath) {
			if (path.endsWith(".java")) {
				search(path);
			}
		}
	}

	public void search(String path) {
		File file = new File(path);
		System.out.println(file.getName().split("\\.")[0]);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				if (matchString(line) != null)
					System.out.println(matchString(line));
				line = reader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String matchString(String line) {
		String result = null;
		if (line.contains("String")) {
			Pattern pattern = Pattern.compile("(?<=\").*?(?=\")");// 匹配双引号中的内容
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				result = matcher.group();
			}
		}
		return result;
	}

	public static void main(String[] args) {
		SearchPrimitive.getInstance().search(
				"C:\\Users\\Flipped\\eclipse-workspace\\evosuite-1.0.6\\client\\src\\test\\java\\org\\evosuite\\PropertiesTest.java");
	}
}
