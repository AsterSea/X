package neu.lab.conflict;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import neu.lab.conflict.util.Conf;
import neu.lab.conflict.writer.SemanticsConflictWriter;

@Mojo(name = "semanticsConflict", defaultPhase = LifecyclePhase.VALIDATE)
public class SemanticsConflictMojo extends ConflictMojo {

	@Override
	public void run() {
		// TODO Auto-generated method stub
//		SemanticsConflictWriter.outPath = Conf.outDir;
//		new SemanticsConflictWriter().writeSemanticsConflict();

		new SemanticsConflictWriter().writeSemanticsConflict(Conf.outDir);
	}
}
