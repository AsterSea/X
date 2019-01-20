package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import neu.lab.conflict.container.Conflicts;
import neu.lab.conflict.container.DepJars;
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
import neu.lab.conflict.vo.DepJar;

public class RiskMethodPathWriter {

	/**
	 * 输出到文件中
	 * 
	 * @param outPath
	 */
	public void writeRiskMethodPathToFile(String outPath) {
		try {
			PrintWriter printer;
			String fileName = MavenUtil.i().getProjectGroupId() + ":" + MavenUtil.i().getProjectArtifactId() + ":"
					+ MavenUtil.i().getProjectVersion();
				printer = new PrintWriter(new BufferedWriter(new FileWriter(outPath + "path_" + fileName.replace('.', '_').replace(':', '_') + ".txt")));

				for (Conflict conflict : Conflicts.i().getConflicts()) {
					List<DepJarJRisk> depJarRisks = conflict.getJarRisks();
					for (DepJarJRisk depJarRisk : depJarRisks) {
						Graph4path pathGraph = depJarRisk.getMethodPathGraphForSemanteme();
						Set<String> hostNds = pathGraph.getHostNds();
						Map<String, IBook> pathBooks = new Dog(pathGraph).findRlt(hostNds, Conf.DOG_DEP_FOR_PATH,
								Strategy.NOT_RESET_BOOK);
						MySortedMap<Integer, Record4path> dis2records = new MySortedMap<Integer, Record4path>();
						for (String topMthd : pathBooks.keySet()) {
							if (hostNds.contains(topMthd)) {
								Book4path book = (Book4path) (pathBooks.get(topMthd));
								for (IRecord iRecord : book.getRecords()) {
									Record4path record = (Record4path) iRecord;
									dis2records.add(record.getPathlen(), record);
								}
							}
						}
						if (dis2records.size() > 0) {
							Set<String> hasWriterRiskMethodPath = new HashSet<String>();
							printer.println("conflict:" + conflict.getSig());
							printer.println("classPath:" + DepJars.i().getUsedJarPathsStr());
							printer.println("pomPath:" + MavenUtil.i().getBaseDir());
							for (Record4path record : dis2records.flat()) {
								if (!hasWriterRiskMethodPath.contains(record.getRiskMthd())) {
									printer.println("pathLen:" + record.getPathlen() + "\n"+ "risk method name:" + record.getRiskMthd() + "\n" + addJarPath(record.getPathStr()));
									hasWriterRiskMethodPath.add(record.getRiskMthd());
								}
								
							}
					}
					}
				}
			printer.close();
		} catch (Exception e) {
			MavenUtil.i().getLog().error("can't write jar duplicate risk:", e);
		} finally{
			
		}
	}
	
	private String addJarPath(String mthdCallPath) {
		StringBuilder sb = new StringBuilder();
		String[] mthds = mthdCallPath.split("\\n");
		for (int i = 0; i < mthds.length - 1; i++) {
			// last method is risk method,don't need calculate.
			String mthd = mthds[i];
			String cls = SootUtil.mthdSig2cls(mthd);
			DepJar depJar = DepJars.i().getClassJar(cls);
			String jarPath = "";
			if (depJar != null)
				jarPath = depJar.getJarFilePaths(true).get(0);
			sb.append(mthd + " " + jarPath + "\n");
		}
		sb.append(mthds[mthds.length - 1]);
		return sb.toString();
	}
}
