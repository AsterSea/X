package neu.lab.conflict;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import neu.lab.conflict.util.Conf;
import neu.lab.conflict.writer.ClassDupRiskWriter;

@Mojo(name = "classDupRisk", defaultPhase = LifecyclePhase.VALIDATE)
public class ClassDupRiskMojo extends ConflictMojo {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (Conf.CLASS_MISSING) {
			new ClassDupRiskWriter().writeByJarHasClassMissing(Conf.outDir);
		}else {
			new ClassDupRiskWriter().writeByJarNoClassMissing(Conf.outDir);
		}
	}

}
