package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.testsuite.TestSuiteChromosome;

import neu.lab.conflict.SemanticsConflictMojo;

public class SemanticsConflictWriter {
	public void writeSemanticsConflict(String outPath) {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(outPath + "SemeanticsConflict.txt", true)));
//			write(printer);
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}// PrintWriter printer

	private void write() {
		EvoSuite evosuite = new EvoSuite();
		String targetClass = SemanticsConflictWriter.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] paths = readFiles("target/dependency/").toArray(new String[] {});
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Set<String> test = new HashSet<String>();
		test.add("-setup");
		test.add("target/classes");
		for (String path:paths) {
			test.add(path.replace('\\', '/'));
		}
		evosuite.parseCommandLine(test.toArray(new String[] {}));
		Object result = evosuite.parseCommandLine(command);
		TestGenerationResult ga = getGAFromResult(result);
//		TestSuiteChromosome test0 = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println(ga.getGeneticAlgorithm());
	}

	public static void main(String[] args) {
		SemanticsConflictWriter sSemanticsConflictWriter = new SemanticsConflictWriter();
		sSemanticsConflictWriter.write();
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
