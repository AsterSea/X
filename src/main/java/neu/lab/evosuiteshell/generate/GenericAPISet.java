package neu.lab.evosuiteshell.generate;

import fj.Hash;
import fj.test.Gen;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.soot.JarAna;
import neu.lab.evosuiteshell.search.MethodInfo;
import neu.lab.evosuiteshell.search.ProjectInfo;
import neu.lab.evosuiteshell.search.SootExe;
import org.evosuite.TestGenerationContext;
import org.evosuite.instrumentation.InstrumentingClassLoader;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

public class GenericAPISet {
    private static GenericAPISet instance = null;

    private InstrumentingClassLoader instrumentingClassLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();
    private HashSet<String> hostAPISig;

    private GenericAPISet() {
        //单例模式，只用soot解析一次host包内所有的classinfo和methodinfo
        String hostJarPath = DepJars.i().getHostDepJar().getJarFilePaths(true).toArray(new String[]{})[0];
        new SootExe().initProjectInfo(new String[]{hostJarPath});
    }

    public static GenericAPISet getInstance() {
        if (instance == null)
            instance = new GenericAPISet();
        return instance;
    }

    public void generateAllAPI() {

        for (MethodInfo methodInfo : ProjectInfo.i().getAllMethod()) {
            if (methodInfo.isPublic()) {
                generateForTargetAPI(methodInfo);
            }
        }
    }

    public void generateForTargetAPI(MethodInfo methodInfo) {
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder();

    }


    //test
    public GenericAPISet(String a) {
        hostAPISig = new HashSet<>();
        //单例模式，只用soot解析一次host包内所有的classinfo和methodinfo
//        String hostJarPath = DepJars.i().getHostDepJar().getJarFilePaths(true).toArray(new String[]{})[0];
//        hostClassesSig = JarAna.i().deconstruct(Arrays.asList(a)).keySet();
        new SootExe().initProjectInfo(new String[]{a});
    }

    public static void main(String[] args) {
        GenericAPISet genericAPISet = new GenericAPISet("/Users/wangchao/eclipse-workspace/Host/target/classes/");
        int i = 0;
        for (MethodInfo methodInfo : ProjectInfo.i().getAllMethod()) {
            if (methodInfo.isPublic()) {
                System.out.println(methodInfo.getSig());
                i++;
            }
        }
        System.out.println(ProjectInfo.i().getAllMethod().size());
        System.out.println(i);
    }
}
