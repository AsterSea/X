package neu.lab.conflict;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "classDupRisk", defaultPhase = LifecyclePhase.VALIDATE)
public class ClassDupRiskMojo extends ConflictMojo {

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
