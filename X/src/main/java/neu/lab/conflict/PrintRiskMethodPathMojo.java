package neu.lab.conflict;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import neu.lab.conflict.util.UserConf;
import neu.lab.conflict.writer.RiskMethodPathWriter;

@Mojo(name = "printRiskMethodPath", defaultPhase = LifecyclePhase.VALIDATE)
public class PrintRiskMethodPathMojo extends ConflictMojo {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		RiskMethodPathWriter riskMethodPathWriter = new RiskMethodPathWriter();
		riskMethodPathWriter.writeRiskMethodPathToFile(UserConf.getOutDir());
	}
}