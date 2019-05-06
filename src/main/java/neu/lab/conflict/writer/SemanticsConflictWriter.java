package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;

import neu.lab.conflict.SemanticsConflictMojo;
import neu.lab.conflict.util.MavenUtil;

public class SemanticsConflictWriter {
	public void writeSemanticsConflict(String outPath) {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(outPath + "SemeanticsConflict.txt", true)));
//			write(printer);
			write();
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}// PrintWriter printer

	private void write() {
//		run();
//		LoggingUtils.changeLogbackFile("logback-evosuite.xml");
		TestSuiteGenerator testSuiteGenerator = new TestSuiteGenerator();
//		EvoSuite evosuite = new EvoSuite();
		String targetClass = "neu.lab.Host.Host";// SemanticsConflictWriter.class.getCanonicalName();
		Properties.getInstance();
		Properties.CP = "C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\classes;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\A-1.0.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\B-1.0.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\evosuite-runtime-1.0.6.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\hamcrest-core-1.3.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\junit-4.12.jar";
//		ClassPathHandler.getInstance().setEvoSuiteClassPath(
//				new String[] { "C:\\Users\\Flipped\\Desktop\\project\\javacpp-1.4.4\\evosuite-master-1.0.6.jar",
//						"C:\\Users\\Flipped\\Desktop\\project\\javacpp-1.4.4\\evosuite-runtime-1.0.6.jar",
//						"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\classes",
//						"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\A-1.0.jar",
//						"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\B-1.0.jar" });
//		Properties.REGRESSIONCP = "C:\\Users\\Flipped\\Desktop\\project\\javacpp-1.4.4\\target\\classes";
		Properties.TARGET_CLASS = targetClass;
		Properties.TARGET_METHOD = "onStart";
//		Properties.LOG_LEVEL = "error";
		System.setProperty("java.class.path",
				"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\classes;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\A-1.0.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\B-1.0.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\evosuite-runtime-1.0.6.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\hamcrest-core-1.3.jar;C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\junit-4.12.jar");
		System.setProperty("org.slf4j.simpleLogger.log.org.evosuite", "debug");
		Properties.CLASSPATH = new String[] { "C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\classes",
				"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\A-1.0.jar",
				"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\B-1.0.jar" };
		Properties.SOURCEPATH = new String[] { "C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\classes",
				"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\A-1.0.jar",
				"C:\\Users\\Flipped\\eclipse-workspace\\Host\\target\\dependency\\B-1.0.jar" };
		TestGenerationResult result = testSuiteGenerator.generateTestSuite();
//		System.out.println(MutationPool.getMutants().get(0).getMutation().getFirst());
//		System.out.println(MutationPool.getMutants().get(0).getMutationName());
//		System.out.println(MutationPool.getMutants());
//		String[] paths = readFiles(".\\target\\dependency\\").toArray(new String[] {});
//		String[] command = new String[] { "-generateSuite", "-class", targetClass };
//		Set<String> test = new HashSet<String>();
//		test.add("-setup");
//		test.add(".\\target\\classes");
//		for (String path:paths) {
//			test.add(path.replace('\\', '/'));
//		}
//		evosuite.parseCommandLine(test.toArray(new String[] {}));
//		Object result = evosuite.parseCommandLine(command);
//		TestGenerationResult ga = getGAFromResult(result);
//		TestSuiteChromosome test0 =  (TestSuiteChromosome) ga.getBestIndividual();
//		System.out.println(result.getTestCode("test0"));
	}

	public static void main(String[] args) {
//		String properties = System.getProperty("java.library.path");
//		String[] paths = properties.split(";");
//		String mvn = "";
//		for (String path : paths) {
//			if (path.contains("maven")) {
//				System.out.println(path);
//				mvn = path + "\\mvn.cmd ";
//			}
//		}
//		System.out.println(properties);
		SemanticsConflictWriter sSemanticsConflictWriter = new SemanticsConflictWriter();
		sSemanticsConflictWriter.write();
//		CommandLine command = CommandLine.parse(mvn + "-version");
//		DefaultExecutor executor = new DefaultExecutor();
//		try {
//			executor.execute(command);
//		} catch (ExecuteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void run() {
		String properties = System.getProperty("java.library.path");
		String[] paths = properties.split(";");
		String mvn = "";
		for (String path : paths) {
			if (path.contains("maven")) {
				System.out.println(path);
				mvn = path + "\\mvn.cmd ";
			}
		}
		System.out.println(properties);
//	SemanticsConflictWriter sSemanticsConflictWriter = new SemanticsConflictWriter();
//	sSemanticsConflictWriter.write();
		CommandLine command = CommandLine.parse(mvn + "-version");
		DefaultExecutor executor = new DefaultExecutor();
		try {
			executor.execute(command);
		} catch (ExecuteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private TestGenerationResult getGAFromResult(Object result) {
		List<List<TestGenerationResult>> results = (List<List<TestGenerationResult>>) result;
		System.out.println(results);
		return results.get(0).get(0);
	}

	public Set<String> readFiles(String path) {
		Set<String> paths = new HashSet<String>();
		File folder = new File(path);
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			paths.add(files[i].getPath());
		}
		return paths;
	}
}
