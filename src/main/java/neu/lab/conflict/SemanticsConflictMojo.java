package neu.lab.conflict;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "semanticsConflict", defaultPhase = LifecyclePhase.VALIDATE)
public class SemanticsConflictMojo extends ConflictMojo {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
