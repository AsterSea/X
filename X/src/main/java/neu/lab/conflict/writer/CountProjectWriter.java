package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import neu.lab.conflict.container.Conflicts;
import neu.lab.conflict.vo.Conflict;

public class CountProjectWriter {

	public void writeToFileForCountInfo(String outPath) {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(outPath + "Conflict.txt", true)));
			writeConflictSig(printer);
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeConflictSig(PrintWriter printer) {
		for (Conflict conflict : Conflicts.i().getConflicts()) {
			printer.println(conflict.getSig());
		}
	}
}
