package neu.lab.conflict;

import neu.lab.conflict.util.Conf;
import neu.lab.conflict.writer.SemanticsPathWriter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "SemanticsPath", defaultPhase = LifecyclePhase.VALIDATE)
public class SemanticsPathMojo extends ConflictMojo {
    @Override
    public void run() {
        new SemanticsPathWriter().writeSemanticsPath(Conf.outDir);
    }
}
