package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.graph.*;
import neu.lab.conflict.util.MySortedMap;
import neu.lab.conflict.vo.DepJar;
import neu.lab.evosuiteshell.generate.GenericObjectSet;
import neu.lab.evosuiteshell.generate.GenericPoolFromTestCase;
import neu.lab.evosuiteshell.search.*;
import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.method.designation.GlobalVar;
import org.evosuite.coverage.method.designation.NodeProbDistance;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.seeding.ConstantPoolManager;
import com.google.common.io.Files;

import neu.lab.conflict.container.Conflicts;
import neu.lab.conflict.distance.MethodProbDistances;
import neu.lab.conflict.graph.Dog.Strategy;
import neu.lab.conflict.risk.jar.DepJarJRisk;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.vo.Conflict;
import neu.lab.conflict.vo.DependencyInfo;
import neu.lab.evosuiteshell.Command;
import neu.lab.evosuiteshell.Config;
import neu.lab.evosuiteshell.ExecuteCommand;
import neu.lab.evosuiteshell.ReadXML;
import neu.lab.evosuiteshell.junit.ExecuteJunit;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.utils.generic.GenericClass;

public class SemanticsConflictWriter {
    private Map<String, IBook> pathBooks;

    private int runTime = 0;
    private boolean hasNecessaryRunNextTime = true;

    public void writeSemanticsConflict() {

        copyDependency();
        copyConflictDependency();
        try {
            for (int time = 0; time < Conf.runTime; time++) {
                runTime++;
                runEvosuite();
                if (!hasNecessaryRunNextTime) {
                    MavenUtil.i().getLog().info("does not have necessary to run next time, exit!");
                    break;
                }
            }
        } catch (Exception e) {
            MavenUtil.i().getLog().error(e.getMessage());
        }
    }

    /**
     * 得到依赖jar包的路径
     */
    private String getDependencyCP(Conflict conflict) {
        StringBuffer CP = new StringBuffer(System.getProperty("user.dir") + Config.FILE_SEPARATOR + "target" + Config.FILE_SEPARATOR + "classes" + Config.CLASSPATH_SEPARATOR + System.getProperty("user.dir") + Config.FILE_SEPARATOR + "target" + Config.FILE_SEPARATOR + "test-classes" + Config.CLASSPATH_SEPARATOR
                + System.getProperty("user.dir") + Config.FILE_SEPARATOR + Config.EVOSUITE_NAME);
        String dependencyConflictJarDir = System.getProperty("user.dir") + Config.FILE_SEPARATOR + Config.SENSOR_DIR + Config.FILE_SEPARATOR
                + "dependencyConflictJar" + Config.FILE_SEPARATOR;
        String dependencyJar = System.getProperty("user.dir") + Config.FILE_SEPARATOR + Config.SENSOR_DIR + Config.FILE_SEPARATOR + "dependencyJar" + Config.FILE_SEPARATOR;
        if (conflict == null) {
            for (String dependency : dependencyJarsPath) {
                CP.append(Config.CLASSPATH_SEPARATOR + dependencyJar + dependency);
            }
        } else {
            for (String dependency : dependencyJarsPath) {
                if (dependency.contains(conflict.getArtifactId()))
                    continue;
                CP.append(Config.CLASSPATH_SEPARATOR + dependencyJar + dependency);
            }
            for (String dependency : dependencyConflictJarsPath) {
                if (dependency.contains(conflict.getArtifactId()))
                    CP.append(Config.CLASSPATH_SEPARATOR + dependencyConflictJarDir + dependency);
            }
        }
        return CP.toString();
    }

    private void runEvosuite() throws Exception {
        HashSet<String> hasDetectConflict = new HashSet<>();
        for (Conflict conflict : Conflicts.i().getConflicts()) {
            if (hasDetectConflict.contains(conflict.getUsedDepJar().toString())) {
                continue;
            }
            hasDetectConflict.add(conflict.getUsedDepJar().toString());
//            new File(Config.SENSOR_DIR + Config.FILE_SEPARATOR + "SemeanticsConflict_" + conflict.getSig().replaceAll("\\p{Punct}", "") + ".txt").createNewFile();
            PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(Config.SENSOR_DIR + Config.FILE_SEPARATOR + "SemeanticsConflict_" + conflict.getSig().replaceAll("\\p{Punct}", "") + runTime + ".txt", Conf.append)));
            printer.println(conflict.toString());
            DepJarJRisk.printer = printer;
//            System.out.println(conflict);
            riskMethodPair(conflict);
            System.setProperty("org.slf4j.simpleLogger.log.org.evosuite", "error");
            for (String method : methodToHost.keySet()) {
//                initObjectPool(SootUtil.mthdSig2cls(method));
//                System.out.println(method);
                String testDir = createMethodDir(method);
                String CP = getDependencyCP(null);
                String ConflictCP = getDependencyCP(conflict);
                String testClassName = "";
                HashSet<String> riskMethodHosts = methodToHost.get(method);
                for (String riskMethodHost : riskMethodHosts) {
                    String riskMethodClassHost = SootUtil.mthdSig2cls(riskMethodHost);
//                    System.out.println(riskMethodHost);
                    printer.println("target class : " + riskMethodClassHost);
                    printer.println("host method : " + riskMethodHost);
                    printer.println("risk method : " + method);
                    testClassName = riskMethodClassHost + "_ESTest";
                    startEvolution(CP, testDir, riskMethodClassHost, method, riskMethodHost);
                    compileJunit(testDir, testClassName, CP);
                    ArrayList<String> results = executeJunit(testDir, testClassName, ConflictCP);
                    printer.println(handleResult(results));
                    printer.println();
                }
                testClassName = SootUtil.mthdSig2cls(method);
                printer.println("risk target class ===> " + testClassName);
                printer.println("risk target method ===> " + method);
                startEvolution(CP, testDir, testClassName, method, method);
                compileJunit(testDir, testClassName + "_ESTest", CP);
                ArrayList<String> results = executeJunit(testDir, testClassName + "_ESTest", ConflictCP);
                printer.println(handleResult(results));
                printer.println("risk method can be called, method path:");
                printer.println(methodPath.get(method));
            }
            printer.println();
            printer.close();
        }
        if (methodToHost.keySet().size() == 0) {
            hasNecessaryRunNextTime = false;
        }
    }

//    public void initObjectPool(String className) {
//        /*
//        测试 getPoolFromJUnit
//         */
////        final org.evosuite.testcarver.extraction.CarvingClassLoader classLoader = new org.evosuite.testcarver.extraction.CarvingClassLoader();
////        InstrumentingClassLoader instrumentingClassLoader = new InstrumentingClassLoader();
////        try {
////            System.out.println(1111);
////            Class<?> Host = instrumentingClassLoader.loadClass(className);
//////            Class<?> Host = instrumentingClassLoader.loadClassFromFile("neu.lab.Host.Host", "/Users/wangchao/eclipse-workspace/Host/target/classes/neu/lab/Host/Host.class");
////            Class<?> Hosttest = instrumentingClassLoader.loadClassFromFile("neu.lab.Host.HostTest", "/Users/wangchao/eclipse-workspace/Host/target/test-classes/neu/lab/Host/HostTest.class");
////            ObjectPool.getPoolFromJUnit(new GenericClass(Host), Hosttest);
////        } catch (ClassNotFoundException e) {
////            e.printStackTrace();
////        }
////Properties.WRITE_POOL="aaa.txt";
//
//        String hostJarPath = DepJars.i().getHostDepJar().getJarFilePaths(true).toArray(new String[]{})[0];
//        if (!(hostJarPath.endsWith(".jar"))) {
//            for (File file : new File(hostJarPath).getParentFile().listFiles()) {
//                if (file.getAbsolutePath().endsWith(".jar")) {
//                    hostJarPath = file.getAbsolutePath();
//                    break;
//                }
//            }
//        }
//        new SootExe().initProjectInfo(new String[]{hostJarPath});
//        ProjectInfo.i().setEntryCls(className);
//        MyClassLoader.jarLoader(new File(hostJarPath));
////    System.out.println(ProjectInfo.i().getClassInfo("neu.lab.A.Principal"));
//        GenericObjectSet.getInstance().generateGenericObject(className);
//    }

    private void seedingConstant(String targetClass) {
        SearchPrimitiveManager.getInstance();
        int num = targetClass.lastIndexOf(".");
        String name = targetClass.substring(num + 1, targetClass.length());
        HashSet<String> constants = SearchConstantPool.getInstance().getPoolValues(name);
        if (constants != null) {
            for (String constant : constants) {
                ConstantPoolManager.getInstance().addSUTConstant(constant);
                ConstantPoolManager.getInstance().addDynamicConstant(constant);
                ConstantPoolManager.getInstance().addNonSUTConstant(constant);
            }
        }
    }

    private void startEvolution(String CP, String testDir, String targetClass, String riskMethod, String byteRiskMethod) {
        Properties.getInstance();
        Properties.CP = CP;
        TestSuiteGenerator testSuiteGenerator = new TestSuiteGenerator();
//        setNodeProbDistance(pathBooks, riskMethod);
//        Properties.SEED_TYPES = false;
        seedingConstant(targetClass);// String 参数种植
//        System.out.println(targetClass);
        GenericPoolFromTestCase.receiveTargetClass(targetClass);


        Properties.ASSERTION_STRATEGY = Properties.AssertionStrategy.ALL;

        Properties.ASSERTION_MINIMIZATION_FALLBACK = 1;
        Properties.ASSERTION_MINIMIZATION_FALLBACK_TIME = 1;

        Properties.MINIMIZE = false;
        Properties.MINIMIZE_VALUES = true;
        Properties.P_OBJECT_POOL = 1;
//        System.out.println(riskMethod);
        Properties.RISK_METHOD = riskMethod;
        Properties.TARGET_METHOD = SootUtil.bytemthdSig2fullymethodName(byteRiskMethod);
//        System.out.println(Properties.TARGET_METHOD);
        Properties.MIN_INITIAL_TESTS = 10;
        Properties.CRITERION = new Criterion[]{Criterion.METHOD, Criterion.BRANCH, Criterion.LINE};
        Properties.NUM_TESTS = 10;
        Properties.TEST_DIR = testDir;
        Properties.JUNIT_CHECK = false;
//        CP = CP.replace(";", ":");// + ":" + System.getProperty("user.dir") + Config.FILE_SEPARATOR + "evosuite-shaded-1.0.6.jar";
//        CP = CP.replaceAll("/Users/wangchao/eclipse-workspace/Host/", "");

//        CP.replaceAll(";",":");
//        System.out.println(CP);
//        System.out.println(targetClass);
        Properties.TARGET_CLASS = targetClass;


//        initObjectPool(targetClass);
//		Properties.TARGET_CLASS = SootUtil.mthdSig2cls(targetClass);
//		Properties.TARGET_METHOD = "onStart";
        testSuiteGenerator.generateTestSuite();
    }

    private void setNodeProbDistance(Map<String, IBook> pathGraph, String riskMethod) {
        MethodProbDistances methodProbabilityDistances = getMethodProDistances(pathGraph);
        NodeProbDistance nodeProbDistance = methodProbabilityDistances.getEvosuiteProbability(riskMethod);
        GlobalVar.i().setNodeProbDistance(nodeProbDistance);
    }

    private MethodProbDistances getMethodProDistances(Map<String, IBook> books) {
        MethodProbDistances distances = new MethodProbDistances();
        for (IBook book : books.values()) {
            // MavenUtil.i().getLog().info("book:"+book.getNodeName());
            for (IRecord iRecord : book.getRecords()) {

                Record4distance record = (Record4distance) iRecord;
                // MavenUtil.i().getLog().info("record:"+record.getName());
                distances.addDistance(record.getName(), book.getNodeName(), record.getDistance());
                distances.addProb(record.getName(), book.getNodeName(), record.getBranch());
            }
        }
        return distances;
    }

    private String handleResult(ArrayList<String> results) {
        double run = 0;
        double failures = 0;
        String handle = "";
        double percent;
        for (String result : results) {
            if (result.contains("Tests")) {
                String[] lines = result.split(",");
                run = Double.parseDouble(lines[0].split(": ")[1]);
                failures = Double.parseDouble(lines[1].split(": ")[1]);
                percent = failures / run;
                handle = "test case nums : " + run + " * failures nums : " + failures + " * failures accounted for "
                        + String.format("%.2f", percent * 100) + "%";
                break;
            }
            if (result.contains("OK")) {
                String line = result.substring(result.indexOf("(") + 1, result.indexOf(")"));
                handle = "test case nums : " + line.split(" ")[0] + " * failures accounted for 0%";
                break;
            }
        }
        return handle;
    }

    private void compileJunit(String testDir, String testClassName, String CP) {
        if (!CP.contains("junit")) {
            CP += addJunitDependency();
        }
        String fileName = testClassName.substring(testClassName.lastIndexOf(".") + 1);
        String packageName = testClassName.replace(fileName, "");
        String fileDir = testDir + packageName.replace(".", Config.FILE_SEPARATOR);
        StringBuffer cmd = new StringBuffer("cd " + fileDir + "\n");
        cmd.append(Command.JAVAC);
        cmd.append(Command.CLASSPATH);
        cmd.append(CP + " ");
        cmd.append("*.java");
//		System.out.println(cmd + "\n" + fileDir);
        ExecuteCommand.exeBatAndGetResult(ExecuteJunit.creatShellScript(cmd.toString(), fileDir));
    }

    private ArrayList<String> executeJunit(String testDir, String testClassName, String CP) {
        if (!CP.contains("junit")) {
            CP += addJunitDependency();
        }
        String fileName = testClassName.substring(testClassName.lastIndexOf(".") + 1);
        String packageName = testClassName.replace(fileName, "");
        String fileDir = testDir + packageName.replace(".", Config.FILE_SEPARATOR);
        StringBuffer cmd = new StringBuffer("cd " + fileDir + "\n");
        cmd.append(Command.JAVA);
        cmd.append(Command.CLASSPATH);
        cmd.append(CP + Config.CLASSPATH_SEPARATOR + testDir);
        cmd.append(Command.JUNIT_CORE);
        cmd.append(testClassName);
//		System.out.println(cmd + "\n" + fileDir);
        return ExecuteCommand.exeBatAndGetResult(ExecuteJunit.creatShellScript(cmd.toString(), fileDir));
    }

    private String addJunitDependency() {
        StringBuffer stringBuffer = new StringBuffer("");
        for (String jarPath : junitJarsPath) {
            stringBuffer.append(Config.CLASSPATH_SEPARATOR);
            stringBuffer.append(jarPath);
        }
        return stringBuffer.toString();
    }

    public Set<String> readFiles(String path) {
        Set<String> paths = new HashSet<String>();
        File folder = new File(path);
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            paths.add(files[i].getPath());
        }
        return paths;
    }

    HashMap<String, HashSet<String>> methodToHost = new HashMap<String, HashSet<String>>();

    HashMap<String, String> methodPath = new HashMap<>();

    /**
     * get Risk method pair to methodToHost
     */
    private void riskMethodPair(Conflict conflict) {
        for (DepJarJRisk depJarRisk : conflict.getJarRisks()) {
            Graph4path pathGraph = depJarRisk.getMethodPathGraphForSemanteme();
            Set<String> hostNodes = pathGraph.getHostNodes();
            /**
             * 可以在这里修改寻找深度，改成10层
             */
            pathBooks = new Dog(pathGraph).findRlt(hostNodes, Conf.DOG_DEP_FOR_PATH, Strategy.NOT_RESET_BOOK);
//			MethodProbDistances methodProbabilityDistances = getMethodProDistances(pathBooks);
//			setNodeProbDistance(methodProbabilityDistances);
//			System.out.println(getMethodProDistances(pathBooks));
//            System.out.println(pathBooks.keySet());
            MySortedMap<Integer, Record4path> dis2records = new MySortedMap<>();

            for (String topMthd : pathBooks.keySet()) {
                if (hostNodes.contains(topMthd)) {
                    Book4path book = (Book4path) pathBooks.get(topMthd);
                    for (IRecord iRecord : book.getRecords()) {
                        Record4path record = (Record4path) iRecord;
                        dis2records.add(record.getPathlen(), record);
                        HashSet<String> host = methodToHost.get(record.getRiskMethod());
                        if (host == null) {
                            host = new HashSet<>();
                            methodToHost.put(record.getRiskMethod(), host);
                        }
                        // 修改后 存的是host节点的完全名
//                        host.add(SootUtil.mthdSig2cls(topMthd));
                        host.add(topMthd);
                    }
//                    Map<String, IBook> books4path = new Dog(pathGraph).findRlt(hostNodes, Conf.DOG_DEP_FOR_PATH,
//                            Strategy.NOT_RESET_BOOK);
//                    Book4path book4path = (Book4path) books4path.get(topMthd);
//                    for (IRecord iRecord : book4path.getRecords()) {
//                        Record4path record4path = (Record4path) iRecord;
//                        dis2records.add(record4path.getPathlen(), record4path);
//                    }
                }
            }
            MavenUtil.i().getLog().info("can reach risk method size : " + methodToHost.keySet().size());
            if (dis2records.size() > 0) {
                for (Record4path record : dis2records.flat()) {
                    methodPath.put(record.getRiskMethod(), addJarPath(record.getPathStr()));
//                    System.out.println(addJarPath(record.getPathStr()));
                }
            }
        }
    }

    private String addJarPath(String mthdCallPath) {
        StringBuilder sb = new StringBuilder();
        String[] mthds = mthdCallPath.split("\\n");
        for (int i = 0; i < mthds.length - 1; i++) {
            // last method is risk method,don't need calculate.
            String mthd = mthds[i];
            String cls = SootUtil.mthdSig2cls(mthd);
            DepJar depJar = DepJars.i().getClassJar(cls);
            String jarPath = "";
            if (depJar != null)
                jarPath = depJar.getJarFilePaths(true).get(0);
            sb.append(mthd + " " + jarPath + "\n");
        }
        sb.append(mthds[mthds.length - 1]);
        return sb.toString();
    }

    /**
     * 创建测试方法目录
     */
    private String createMethodDir(String riskMethod) {
        String workPath = System.getProperty("user.dir") + Config.FILE_SEPARATOR + Config.SENSOR_DIR + Config.FILE_SEPARATOR + "test_method" + runTime + Config.FILE_SEPARATOR;
        riskMethod = SootUtil.bytemthdSig2methodName(riskMethod);
        workPath = workPath + riskMethod + Config.FILE_SEPARATOR;
        File dir = new File(workPath);
        if (!dir.exists())
            dir.mkdirs();
        return workPath;
    }

    private String[] dependencyJarsPath;
    private String[] junitJarsPath;

    /**
     * 复制项目自身的依赖到指定文件夹中
     */
    private void copyDependency() {
        String workPath = System.getProperty("user.dir") + Config.FILE_SEPARATOR + Config.SENSOR_DIR + Config.FILE_SEPARATOR;
        String dependencyJarDir = workPath + "dependencyJar" + Config.FILE_SEPARATOR;
        String targetPom = MavenUtil.i().getProjectPom();
        if (!(new File(dependencyJarDir)).exists()) {
            new File(dependencyJarDir).mkdirs();
        }
        String mvnCmd = Config.getMaven() + Command.MVN_POM + targetPom + Command.MVN_COPY + dependencyJarDir;
        try {
            ExecuteCommand.exeCmd(mvnCmd);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        File file = new File(dependencyJarDir);
        boolean existJunit = false;
        for (String dependency : file.list()) {
            if (dependency.contains("junit")) {
                existJunit = true;
                break;
            }
        }
        if (!existJunit) {
            copyJunitFormMaven();
        }
        dependencyJarsPath = file.list();
    }

    /**
     * 依赖中没有junit包，则手动导入Junit4-12的包依赖
     */
    private void copyJunitFormMaven() {
        String workPath = System.getProperty("user.dir") + Config.FILE_SEPARATOR + Config.SENSOR_DIR + Config.FILE_SEPARATOR + "junit" + Config.FILE_SEPARATOR;
        if (!(new File(workPath)).exists()) {
            new File(workPath).mkdirs();
        }
        String xmlFileName = ReadXML.copyPom(ReadXML.COPY_JUNIT);
        ReadXML.executeMavenCopy(xmlFileName, workPath);
        junitJarsPath = new File(workPath).list();
        for (int i = 0; i < junitJarsPath.length; i++) {
            junitJarsPath[i] = workPath + junitJarsPath[i];
        }
    }

    private String[] dependencyConflictJarsPath;

    /**
     * 复制项目中自身冲突的依赖到指定文件夹中 升级更新：不复制到指定文件夹，直接定位到仓库位置
     */
    private void copyConflictDependency() {
        String dependencyConflictJarDir = System.getProperty("user.dir") + Config.FILE_SEPARATOR + Config.SENSOR_DIR + Config.FILE_SEPARATOR
                + "dependencyConflictJar" + Config.FILE_SEPARATOR;
        if (!(new File(dependencyConflictJarDir)).exists()) {
            new File(dependencyConflictJarDir).mkdirs();
        }
        String conflictDependencyJar = MavenUtil.i().getMvnRep();
        for (Conflict conflict : Conflicts.i().getConflicts()) {
            try {
                for (DepJarJRisk depJarJRisk : conflict.getJarRisks()) {
                    conflictDependencyJar += conflict.getGroupId().replace(".", Config.FILE_SEPARATOR) + Config.FILE_SEPARATOR
                            + conflict.getArtifactId().replace(".", Config.FILE_SEPARATOR) + Config.FILE_SEPARATOR + depJarJRisk.getVersion() + Config.FILE_SEPARATOR
                            + conflict.getArtifactId() + "-" + depJarJRisk.getVersion() + ".jar";
                    if (new File(conflictDependencyJar).exists()) {
                        try {
                            Files.copy(new File(conflictDependencyJar), new File(dependencyConflictJarDir + Config.FILE_SEPARATOR
                                    + conflict.getArtifactId() + "-" + depJarJRisk.getVersion() + ".jar"));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        DependencyInfo dependencyInfo = new DependencyInfo(conflict.getGroupId(), conflict.getArtifactId(),
                                depJarJRisk.getVersion());
                        copyConflictFromMaven(dependencyInfo, dependencyConflictJarDir);
                    }
                }
            } catch (Exception e) {
                MavenUtil.i().getLog().error(e.getMessage());
            }
        }
        dependencyConflictJarsPath = new File(dependencyConflictJarDir).list();
    }

    private void copyConflictFromMaven(DependencyInfo dependencyInfo, String dir) {
        String xmlFileName = ReadXML.copyPom(ReadXML.COPY_CONFLICT);
        ReadXML.setCopyDependency(dependencyInfo, xmlFileName);
        ReadXML.executeMavenCopy(xmlFileName, dir);
    }
}