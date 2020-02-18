package neu.lab.conflict;

import neu.lab.conflict.util.Conf;
import neu.lab.conflict.writer.SemanticsConflictSupImplWriter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "SemanticsConflictSupImpl", defaultPhase = LifecyclePhase.VALIDATE)
public class SemanticsConflictSupImplMojo extends ConflictMojo {
    @Override
    public void run() {
        new SemanticsConflictSupImplWriter().writeSemanticsPath(Conf.outDir);
    }
}
