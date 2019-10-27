package neu.lab.evosuiteshell.generate;

import neu.lab.conflict.util.MavenUtil;
import neu.lab.evosuiteshell.Config;
import org.apache.maven.Maven;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.utils.generic.GenericClass;

import java.io.File;

public class GenericPoolFromTestCase {

    private static final org.evosuite.testcarver.extraction.CarvingClassLoader classLoader = new org.evosuite.testcarver.extraction.CarvingClassLoader();

    private static InstrumentingClassLoader instrumentingClassLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();

    public static void receiveTargetClass(String fullyQualifiedName) {
        File dir = new File(Config.PROJECT_TESTCASE_DIR);
        if (!dir.exists()) {
            MavenUtil.i().getLog().info("project does not have test case");
            return;
        }
        String packageName = fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf('.') + 1);
        String className = fullyQualifiedName.replace(packageName, "");
        String packagePath = dir.getPath() + "/" + packageName.replace('.', '/');
        File packageDir = new File(packagePath);
        if (!packageDir.exists()) {
            MavenUtil.i().getLog().info("project does not have " + fullyQualifiedName + " test case");
            return;
        }
        File[] testFiles = packageDir.listFiles();
        for (File testFile : testFiles) {
            if (testFile.getName().contains(className)) {
                String testName = testFile.getName().replace(".java", "");
                genericPool(packageName, className, testName);
            }
        }
    }


    private static void genericPool(String packageName, String className, String testName) {
//        System.out.println(packageName);
//        System.out.println(className);
//        System.out.println(testName);
        String targetClass = packageName + className;
        String selectedJunit = packageName + testName;
        Properties.TARGET_CLASS = targetClass;
        Properties.SELECTED_JUNIT = selectedJunit;
        Class<?> objectClass = null;
        Class<?> objectTestClass = null;
        try {
//            System.out.println(targetClass);
            objectClass = instrumentingClassLoader.loadClass(targetClass);
            objectTestClass = classLoader.loadClass(selectedJunit);
        } catch (Exception e) {
            MavenUtil.i().getLog().error("load class error " + e.getMessage());
            e.printStackTrace();
            return;
        }
        ObjectPool pool = ObjectPool.getPoolFromJUnit(new GenericClass(objectClass), objectTestClass);
        MavenUtil.i().getLog().info("success get pool. size : " + pool.getNumberOfSequences());
        ObjectPoolManager.getInstance().addPool(pool);
//        Properties.P_OBJECT_POOL = 0.5;
    }

    public static void main(String[] args) {
        GenericPoolFromTestCase genericPoolFromTestCase = new GenericPoolFromTestCase();
        genericPoolFromTestCase.receiveTargetClass("neu.lab.evosuiteshell.junit.My");
//        File file = new File("/");
//        if (file.exists()) {
//            for (File f : file.listFiles()) {
//                System.out.println(f.getPath());
//            }
//        }

    }
}
