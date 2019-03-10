package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import neu.lab.conflict.vo.Conflict;

public class CountProjectWriter {

	private List<Conflict> conflicts;

	public CountProjectWriter() {
		conflicts = Conflicts.i().getConflicts();
	}

	public void writeToFileForCountInfo(String outPath) {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(outPath + "ConflictCount.txt", true)));
			writeConflictSig(printer);
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeForRiskMethodInProject(String outPath) {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(outPath + "RiskMethodCount.txt", true)));
			writeRiskMethodCountInProject(printer);
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeConflictSig(PrintWriter printer) {
		for (Conflict conflict : conflicts) {
			printer.println(conflict.getSig());
		}
	}

	private void writeRiskMethodCountInProject(PrintWriter printer) {
		printer.println("projectInfo:" + MavenUtil.i().getProjectCor());
		for (Conflict conflict : conflicts) {
			printer.println("conflictSig:" + conflict.getSig());
			for (DepJarJRisk depJarRisk : conflict.getJarRisks()) {
				Graph4path pathGraph = depJarRisk.getMethodPathGraphForSemanteme();
				Set<String> hostNodes = pathGraph.getHostNodes();
				Map<String, IBook> pathBooks = new Dog(pathGraph).findRlt(hostNodes, Conf.DOG_DEP_FOR_PATH,
						Strategy.NOT_RESET_BOOK);
				MySortedMap<Integer, Record4path> dis2records = new MySortedMap<Integer, Record4path>();
				for (String topMthd : pathBooks.keySet()) {
					if (hostNodes.contains(topMthd)) {
						Book4path book = (Book4path) (pathBooks.get(topMthd));
						for (IRecord iRecord : book.getRecords()) {
							Record4path record = (Record4path) iRecord;
							dis2records.add(record.getPathlen(), record);
						}
					}
				}
				Map<String, Integer> semantemeMethodForDifferences = depJarRisk.getSemantemeMethodForDifferences();
				if (dis2records.size() > 0) {
					for (Record4path record : dis2records.flat()) {
						int difference = semantemeMethodForDifferences.get(record.getRiskMthd());
						printer.println("riskMethod:" + record.getRiskMthd());
						printer.println("difference:" + difference);
					}
				}
			}
		}
		printer.println();
	}
}
