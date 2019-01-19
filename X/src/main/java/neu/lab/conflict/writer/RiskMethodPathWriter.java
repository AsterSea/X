package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;


import neu.lab.conflict.util.MavenUtil;

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

				
				
			printer.println("test");
			
			printer.close();
		} catch (Exception e) {
			MavenUtil.i().getLog().error("can't write jar duplicate risk:", e);
		}
	}

}
