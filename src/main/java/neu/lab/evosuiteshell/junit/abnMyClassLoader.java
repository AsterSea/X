package neu.lab.evosuiteshell.junit;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class abnMyClassLoader extends ClassLoader {
	@Override
	protected Class<?> findClass(String name) {
		String myPath = "C:/Users/Flipped/eclipse-workspace/Host/evosuite-tests/" + name.replace(".", "/") + ".class";
		System.out.println(myPath);
		byte[] cLassBytes = null;
		Path path = null;
		try {
			path = Paths.get(myPath);
			cLassBytes = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Class clazz = defineClass(name, cLassBytes, 0, cLassBytes.length);
		return clazz;
	}

	public static void main(String[] args) throws ClassNotFoundException {
		System.setProperty("java.class.path",
				"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\classes;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\junit-4.12.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\A-1.0.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\evosuite-runtime-1.0.6.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\evosuite-tests;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\hamcrest-core-1.3.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\B-2.0.jar");
		abnMyClassLoader loader = new abnMyClassLoader();
		Class<?> aClass = loader.findClass("B.B.ServicesConfig_ESTest");
		loadJar("C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\");
		try {
			Object obj = aClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void loadJar(String path) {
		// 系统类库路径
		File libPath = new File(path);

		// 获取所有的.jar和.zip文件
		File[] jarFiles = libPath.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar") || name.endsWith(".zip");
			}
		});

		if (jarFiles != null) {
			// 从URLClassLoader类中获取类所在文件夹的方法
			// 对于jar文件，可以理解为一个存放class文件的文件夹
			Method method = null;
			try {
				method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			boolean accessible = method.isAccessible(); // 获取方法的访问权限
			try {
				if (accessible == false) {
					method.setAccessible(true); // 设置方法的访问权限
				}
				// 获取系统类加载器
				URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
				for (File file : jarFiles) {
					try {
						URL url = file.toURI().toURL();
						method.invoke(classLoader, url);
						System.out.println("读取jar文件成功" + file.getName());
					} catch (Exception e) {
						System.out.println("读取jar文件失败");
					}
				}
			} finally {
				method.setAccessible(accessible);
			}
		}
	}
}
