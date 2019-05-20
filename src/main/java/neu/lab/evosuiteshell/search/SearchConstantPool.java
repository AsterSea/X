package neu.lab.evosuiteshell.search;

import java.util.HashMap;
import java.util.HashSet;

public class SearchConstantPool {
	private static SearchConstantPool instance = new SearchConstantPool();

	private SearchConstantPool() {

	}

	public static SearchConstantPool getInstance() {
		return instance;
	}

	private HashMap<String, HashSet<String>> pool = new HashMap<String, HashSet<String>>();

	public void setPool(String name, String value) {
		HashSet<String> values = pool.get(name);
		if (values == null) {
			values = new HashSet<String>();
			pool.put(name, values);
		}
		values.add(value);
	}

	public HashSet<String> getPoolValues(String name) {
		return pool.get(name);
	}
}
