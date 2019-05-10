package neu.lab.evosuiteshell.junit;

import java.util.ArrayList;

import neu.lab.evosuiteshell.Command;
import neu.lab.evosuiteshell.ExecuteCommand;

public class ExecuteJunit {
	public static ArrayList<String> compileTestCaseJava(String CP, String testName) {
		String command = Command.JAVAC + Command.CLASSPATH + CP + testName;
		System.out.println(command);
		return ExecuteCommand.exeCmdAndGetResult("C:\\Program Files\\Java\\jdk1.8.0_191\\jre\\java");
	}

	public static void main(String[] args) {
		System.out.println(System.getProperty("java.home"));
		System.out.println("Java编译器：" + System.getProperty("java.compiler")); // Java编译器
		System.out.println("Java执行路径：" + System.getProperty("java.ext.dirs")); // Java执行路径

		System.setProperty("user.dir", "C:\\Users\\Flipped\\eclipse-workspace\\Host\\evosuite-tests\\B\\B");
		ArrayList<String> result = compileTestCaseJava(
				"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\classes;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\junit-4.12.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\A-1.0.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\evosuite-runtime-1.0.6.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\evosuite-tests;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\hamcrest-core-1.3.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\B-1.0.jar",
				" ServicesConfig_ESTest.java");
		System.out.println(result);
	}
}
