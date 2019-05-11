package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import com.google.common.io.Files;

import neu.lab.conflict.SemanticsConflictMojo;
import neu.lab.conflict.container.Conflicts;
import neu.lab.conflict.graph.Book4path;
import neu.lab.conflict.graph.Dog;
import neu.lab.conflict.graph.Graph4path;
import neu.lab.conflict.graph.IBook;
import neu.lab.conflict.graph.IRecord;
import neu.lab.conflict.graph.Record4path;
import neu.lab.conflict.graph.Dog.Strategy;
import neu.lab.conflict.risk.jar.DepJarJRisk;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.MySortedMap;
import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.vo.Conflict;
import neu.lab.evosuiteshell.Command;
import neu.lab.evosuiteshell.Config;
import neu.lab.evosuiteshell.ExecuteCommand;
import neu.lab.evosuiteshell.junit.ExecuteJunit;

public class SemanticsConflictWriter {
	public void writeSemanticsConflict(String outPath) {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(outPath + "SemeanticsConflict.txt", true)));
//			write(printer);
//			write();
			runEvosuite(printer);
//			createMethodDir();
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}// PrintWriter printer

	/**
	 * 得到依赖jar包的路径
	 */
	public String getDependencyCP(Conflict conflict) {
		copyDependency();
		StringBuffer CP = new StringBuffer(System.getProperty("user.dir") + "\\target\\classes;"
				+ System.getProperty("user.dir") + "\\" + Config.EVOSUITE_NAME);
		String dependencyConflictJarDir = System.getProperty("user.dir") + "\\" + Config.SENSOR_DIR + "\\"
				+ "defependencyConflictJar\\";
		String dependencyJar = System.getProperty("user.dir") + "\\" + Config.SENSOR_DIR + "\\" + "dependencyJar\\";
		if (conflict == null) {
			for (String dependency : dependencyJarsPath) {
				CP.append(";" + dependencyJar + dependency);
			}
		} else {
			copyConflictDependency();
			for (String dependency : dependencyJarsPath) {
				if (dependency.contains(conflict.getArtifactId()))
					continue;
				CP.append(";" + dependencyJar + dependency);
			}
			for (String dependency : dependencyConflictJarsPath) {
				if (dependency.contains(conflict.getArtifactId()))
					CP.append(";" + dependencyConflictJarDir + dependency);
			}
		}
		return CP.toString();
	}

	public void runEvosuite(PrintWriter printer) {
		for (Conflict conflict : Conflicts.i().getConflicts()) {
			System.out.println(conflict.toString());
			riskMethodPair(conflict);
			System.setProperty("org.slf4j.simpleLogger.log.org.evosuite", "error");
			for (String method : methodToHost.keySet()) {
				HashSet<String> riskMethodHosts = methodToHost.get(method);
				for (String riskMethodHost : riskMethodHosts) {
					printer.println(conflict.toString());
					printer.println(riskMethodHost + "====>" + method);
					String testClassName = SootUtil.mthdSig2cls(riskMethodHost) + "_ESTest";
					String testDir = createMethodDir(method);
					TestSuiteGenerator testSuiteGenerator = new TestSuiteGenerator();
					String CP = getDependencyCP(null);
					String ConflictCP = getDependencyCP(conflict);
					Properties.getInstance();
					Properties.TEST_DIR = testDir;
					Properties.CP = CP + ";" + System.getProperty("user.dir") + "\\" + Config.EVOSUITE_NAME;
					Properties.TARGET_CLASS = SootUtil.mthdSig2cls(riskMethodHost);
					Properties.TARGET_METHOD = SootUtil.mthdSig2methodName(riskMethodHost);
					testSuiteGenerator.generateTestSuite();
					compileJunit(testDir, testClassName, CP);
					ArrayList<String> results = executeJunit(testDir, testClassName, ConflictCP);
					printer.println(handleResult(results));
				}
			}
		}
	}

	public String handleResult(ArrayList<String> results) {
		double run = 0;
		double failures = 0;
		String handle = "";
		double percent = 1;
		for (String result : results) {
			if (result.contains("Tests")) {
				String[] lines = result.split(",");
				run = Double.parseDouble(lines[0].split(": ")[1]);
				failures = Double.parseDouble(lines[1].split(": ")[1]);
				percent = failures / run;
				handle = "test case nums : " + run + " failures nums : " + failures + "\nFailures accounted for "
						+ percent * 100 + "%";
				break;
			}
			if (result.contains("OK")) {
				String line = result.substring(result.indexOf("(") + 1, result.indexOf(")"));
				handle = "test case nums : " + line.split(" ")[0] + "\nFailures accounted for 0%";
				break;
			}
		}
		return handle;
	}

	public void compileJunit(String testDir, String testClassName, String CP) {
		String fileName = testClassName.substring(testClassName.lastIndexOf(".") + 1);
		String packageName = testClassName.replace(fileName, "");
		String fileDir = testDir + packageName.replace(".", "\\");
		StringBuffer cmd = new StringBuffer("cd " + fileDir + "\n");
		cmd.append(Command.JAVAC);
		cmd.append(Command.CLASSPATH);
		cmd.append(CP + " ");
		cmd.append("*.java");
		System.out.println(cmd + "\n" + fileDir);
		ExecuteCommand.exeCmdAndGetResult(ExecuteJunit.creatBat(cmd.toString(), fileDir));
	}

	public ArrayList<String> executeJunit(String testDir, String testClassName, String CP) {
		String fileName = testClassName.substring(testClassName.lastIndexOf(".") + 1);
		String packageName = testClassName.replace(fileName, "");
		String fileDir = testDir + packageName.replace(".", "\\");
		StringBuffer cmd = new StringBuffer("cd " + fileDir + "\n");
		cmd.append(Command.JAVA);
		cmd.append(Command.CLASSPATH);
		cmd.append(CP + ";" + testDir);
		cmd.append(Command.JUNIT_CORE);
		cmd.append(testClassName);
		System.out.println(cmd + "\n" + fileDir);
		return ExecuteCommand.exeCmdAndGetResult(ExecuteJunit.creatBat(cmd.toString(), fileDir));
	}

	public static void main(String[] args) {
//		String testClassName = "B.B.ServicesConfig_ESTest";
//		String fileName = testClassName.substring(testClassName.lastIndexOf(".") + 1);
//		String packageName = testClassName.replace(fileName, "").replace(".", "\\");
//		System.out.println(packageName);
		String result = "(3 tests)";
		String lines = result.substring(result.indexOf("(") + 1, result.indexOf(")"));
		System.out.println(lines.split(" ")[0]);
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

	HashMap<String, HashSet<String>> methodToHost = new HashMap<String, HashSet<String>>();

	/**
	 * get Risk method pair to methodToHost
	 */
	public void riskMethodPair(Conflict conflict) {
		for (DepJarJRisk depJarRisk : conflict.getJarRisks()) {
			Graph4path pathGraph = depJarRisk.getMethodPathGraphForSemanteme();
			Set<String> hostNodes = pathGraph.getHostNodes();
			Map<String, IBook> pathBooks = new Dog(pathGraph).findRlt(hostNodes, Conf.DOG_DEP_FOR_PATH,
					Strategy.NOT_RESET_BOOK);
			for (String topMthd : pathBooks.keySet()) {
				if (hostNodes.contains(topMthd)) {
					Book4path book = (Book4path) (pathBooks.get(topMthd));
					for (IRecord iRecord : book.getRecords()) {
						Record4path record = (Record4path) iRecord;
						HashSet<String> host = methodToHost.get(record.getRiskMthd());
						if (host == null) {
							host = new HashSet<String>();
							methodToHost.put(record.getRiskMthd(), host);
						}
						host.add(topMthd);
					}
				}
			}
			System.out.println(methodToHost);
		}
	}

	/**
	 * 创建测试方法目录
	 */
	public String createMethodDir(String riskMethod) {
		String workPath = System.getProperty("user.dir") + "\\" + Config.SENSOR_DIR + "\\test_method\\";
		riskMethod = SootUtil.mthdSig2methodName(riskMethod);
		workPath = workPath + riskMethod + "\\";
		File dir = new File(workPath);
		if (!dir.exists())
			dir.mkdirs();
		try {
			new File(workPath + riskMethod + ".txt").createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workPath;
	}

	private String[] dependencyJarsPath;

	/**
	 * 复制项目自身的依赖到指定文件夹中
	 */
	public void copyDependency() {
		String workPath = System.getProperty("user.dir") + "\\" + Config.SENSOR_DIR + "\\";
		String dependencyJarDir = workPath + "dependencyJar\\";
		String targetPom = MavenUtil.i().getProjectPom();
		if (!(new File(dependencyJarDir)).exists()) {
			new File(dependencyJarDir).mkdirs();
		}
		String mvnCmd = Config.getMaven() + Command.MVN_POM + targetPom + Command.MVN_COPY + dependencyJarDir;
		try {
			ExecuteCommand.exeCmd(mvnCmd);
		} catch (ExecuteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File file = new File(dependencyJarDir);
		dependencyJarsPath = file.list();
	}

	private String[] dependencyConflictJarsPath;

	/**
	 * 复制项目中自身冲突的依赖到指定文件夹中 升级更新：不复制到指定文件夹，直接定位到仓库位置
	 */
	public void copyConflictDependency() {
		String dependencyConflictJarDir = System.getProperty("user.dir") + "\\" + Config.SENSOR_DIR + "\\"
				+ "defependencyConflictJar\\";
		if (!(new File(dependencyConflictJarDir)).exists()) {
			new File(dependencyConflictJarDir).mkdirs();
		}
		String conflictDependencyJar = MavenUtil.i().getMvnRep();
		for (Conflict conflict : Conflicts.i().getConflicts()) {
			for (DepJarJRisk depJarJRisk : conflict.getJarRisks()) {
				conflictDependencyJar += conflict.getGroupId().replace(".", "\\") + "\\"
						+ conflict.getArtifactId().replace(".", "\\") + "\\" + depJarJRisk.getVersion() + "\\"
						+ conflict.getArtifactId() + "-" + depJarJRisk.getVersion() + ".jar";
				try {
					Files.copy(new File(conflictDependencyJar), new File(dependencyConflictJarDir + "\\"
							+ conflict.getArtifactId() + "-" + depJarJRisk.getVersion() + ".jar"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		dependencyConflictJarsPath = new File(dependencyConflictJarDir).list();
	}
}