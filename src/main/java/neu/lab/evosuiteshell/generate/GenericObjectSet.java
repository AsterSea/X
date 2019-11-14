package neu.lab.evosuiteshell.generate;

import fj.Hash;
import fj.P;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.ClassVO;
import neu.lab.evosuiteshell.search.*;
import org.eclipse.core.internal.resources.Project;
import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.method.designation.EvosuiteNeedObject;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClass;
import soot.PackManager;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class GenericObjectSet {

    private InstrumentingClassLoader instrumentingClassLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();

    private TestCaseBuilder testCaseBuilder;

    public void generateObject(String targetClass) {
//        testCaseBuilder = new TestCaseBuilder();
        Class<?> targetClazz;
        try {
            targetClazz = instrumentingClassLoader.loadClass(targetClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        ClassInfo targetClassInfo = ProjectInfo.i().getClassInfo(targetClass);
        if (targetClassInfo == null) {
            return;
        }
        ProjectInfo.i().setEntryCls(targetClass);
        List<MethodInfo> methodInfoList = targetClassInfo.getAllConstructorContainsChildren();

        //添加所有返回值为target class的方法
        for (MethodInfo methodInfo : ProjectInfo.i().getAllMethod()) {
            if (methodInfo.getReturnType().equals(targetClass)) {
                if (methodInfoList.contains(methodInfo)) {
                    continue;
                }
                methodInfoList.add(methodInfo);
            }
        }
        //methodInfoList 包括所有可用的构造方法和返回值为target class的方法
        int num = 0;

        for (MethodInfo methodInfo : methodInfoList) {
            boolean generate = false;
            if (num > 10) break;

            if (methodInfo.getCls().getSig().equals(targetClass)) {
                generate = generate(targetClassInfo, methodInfo);
            } else if (methodInfo.getReturnType().equals(targetClass)) {

            }
            if (generate) {
                num++;
            }
        }

    }

    private boolean generate(ClassInfo classInfo, MethodInfo methodInfo) {
        testCaseBuilder = new TestCaseBuilder();
        List<NeededObj> neededParams = new ArrayList<>();
        for (String paramType : methodInfo.getParamTypes()) {
            neededParams.add(new NeededObj(paramType, 0));
        }
        VariableReference variableReference = structureParamTypes(testCaseBuilder, classInfo, neededParams);
        if (variableReference == null) {
            return false;
        } else {
            ObjectPool objectPool = new ObjectPool();
            try {
                objectPool.addSequence(new GenericClass(instrumentingClassLoader.loadClass(classInfo.getSig())), testCaseBuilder.getDefaultTestCase());
            } catch (Exception e) {
//                e.printStackTrace();
                MavenUtil.i().getLog().error(e);
                return false;
            }
            ObjectPoolManager.getInstance().addPool(objectPool);
            return true;
        }
    }


    private static GenericObjectSet instance = null;

    private GenericObjectSet() {
        //单例模式，只用soot解析一次host包内所有的classinfo和methodinfo
        String hostJarPath = DepJars.i().getHostDepJar().getJarFilePaths(true).toArray(new String[]{})[0];
        new SootExe().initProjectInfo(new String[]{hostJarPath});
    }

    private GenericObjectSet(String a) {
        //测试方法
        //单例模式，只用soot解析一次host包内所有的classinfo和methodinfo
//        String hostJarPath = DepJars.i().getHostDepJar().getJarFilePaths(true).toArray(new String[]{})[0];
        new SootExe().initProjectInfo(new String[]{a});
    }

    public static GenericObjectSet getInstance() {
        if (instance == null)
            instance = new GenericObjectSet();
        return instance;
    }


    public void generateGenericObject(String classSig) {
        System.out.println(classSig);
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder();
        ClassInfo classInfo = ProjectInfo.i().getClassInfo(classSig);
        if (classInfo == null) {
            return;
        }
////        System.out.println(classSig + "asdf");
        List<MethodInfo> methodInfoList = classInfo.getAllConstructorContainsChildren();
////        System.out.println(methodInfo.getSig());
////        List<String> paramTypes = methodInfo.getParamTypes();
        List<NeededObj> neededParams = new ArrayList<NeededObj>();
        for (MethodInfo methodInfo : methodInfoList) {
            for (String paramType : methodInfo.getParamTypes()) {
                neededParams.add(new NeededObj(paramType, 0));
            }
//            EvosuiteNeedObject evosuiteNeedParamObject = structureParamTypes(classInfo, neededParams);
////            System.out.println(evosuiteNeedParamObject.toString());
        }
//        VariableReference variableReference = structureParamTypes(testCaseBuilder, classInfo, neededParams);
        ObjectPool objectPool = new ObjectPool();

//        TestCaseBuilder t = new TestCaseBuilder();

//        InstrumentingClassLoader instrumentingClassLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();
//        Class<?> clazzzzz=null;
//        try {
//            clazzzzz=instrumentingClassLoader.loadClassFromFile("neu.lab.Host.Host","/Users/wangchao/eclipse-workspace/Host/target/classes/neu/lab/Host/Host.class");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        try {
            objectPool.addSequence(new GenericClass(instrumentingClassLoader.loadClass(classSig)), testCaseBuilder.getDefaultTestCase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ObjectPoolManager.getInstance().addPool(objectPool);
//        System.out.println(testCaseBuilder.getDefaultTestCase().getStatement(1).getReturnValue().getType().getTypeName());


//        TestCase sequence = objectPool.getRandomSequence(new GenericClass(MyClassLoader.loaderClass(classInfo.getSig())));
//        System.out.println(sequence.toCode());
//        System.out.println("\n" + testCaseBuilder.toCode());
    }

//    public EvosuiteNeedObject structureParamTypes(ClassInfo classInfo, List<NeededObj> neededObjList) {
////        EvosuiteNeedObject evosuiteNeedObject = new EvosuiteNeedObject(classInfo.getSig());
////        for (NeededObj neededObj : neededObjList) {
////            if (neededObj.isSimpleType()) {
////                evosuiteNeedParamObject.addEvosuiteNeedParamObject(new EvosuiteNeedObject(neededObj.getClassSig()));
////            } else {
////                MethodInfo methodInfo = neededObj.getClassInfo().getBestCons(false);
////                    evosuiteNeedParamObject.addEvosuiteNeedParamObject(structureParamTypes(neededObj.getClassInfo(), neededObj.getConsParamObs(methodInfo)));
////            }
////        }
////        return evosuiteNeedParamObject;
//        return null;
//    }

    //构建参数列表
    public VariableReference structureParamTypes(TestCaseBuilder testCaseBuilder, ClassInfo classInfo, List<NeededObj> neededObjList) {
        List<VariableReference> variableReferenceList = new ArrayList<VariableReference>();
        List<Class<?>> classList = new ArrayList<Class<?>>();
        for (NeededObj neededObj : neededObjList) {
            VariableReference variableReference = null;
            Class<?> type = null;
            if (neededObj.isSimpleType()) {
                switch (neededObj.getClassSig()) {
                    case "boolean":
                        variableReference = testCaseBuilder.appendBooleanPrimitive(Randomness.nextBoolean());
                        type = boolean.class;
                        break;
                    case "byte":
                        variableReference = testCaseBuilder.appendBytePrimitive(Randomness.nextByte());
                        type = byte.class;
                        break;
                    case "char":
                        variableReference = testCaseBuilder.appendCharPrimitive(Randomness.nextChar());
                        type = char.class;
                        break;
                    case "short":
                        variableReference = testCaseBuilder.appendShortPrimitive(Randomness.nextShort());
                        type = short.class;
                        break;
                    case "int":
                        variableReference = testCaseBuilder.appendIntPrimitive(Randomness.nextInt());
                        type = int.class;
                        break;
                    case "long":
                        variableReference = testCaseBuilder.appendLongPrimitive(Randomness.nextLong());
                        type = long.class;
                        break;
                    case "float":
                        variableReference = testCaseBuilder.appendFloatPrimitive(Randomness.nextFloat());
                        type = float.class;
                        break;
                    case "double":
                        variableReference = testCaseBuilder.appendDoublePrimitive(Randomness.nextDouble());
                        type = double.class;
                        break;
                    case "java.lang.String":
                        variableReference = testCaseBuilder.appendStringPrimitive(Randomness.nextString(Randomness.nextInt()));
                        type = String.class;
                        break;
                }
                if (variableReference != null) {
                    variableReferenceList.add(variableReference);
                    classList.add(type);
                }
            } else {//不是简单类型
                try {
                    type = instrumentingClassLoader.loadClass(classInfo.getSig());
                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
                    MavenUtil.i().getLog().error(e);
                    return null;
                }
                classList.add(type);
                MethodInfo bestConcrete = neededObj.getClassInfo().getBestCons(false);
                variableReferenceList.add(structureParamTypes(testCaseBuilder, neededObj.getClassInfo(), neededObj.getConsParamObs(bestConcrete)));
            }
        }
        VariableReference variableReferenceConstructor = null;
        Class<?> clazz = null;
        try {
            clazz = instrumentingClassLoader.loadClass(classInfo.getSig());
            variableReferenceConstructor = testCaseBuilder.appendConstructor(clazz.getConstructor(classList.toArray(new Class<?>[]{})), variableReferenceList.toArray(new VariableReference[]{}));
        } catch (Exception e) {
            MavenUtil.i().getLog().error(e);
            return null;
        }

        return variableReferenceConstructor;
    }

    public static void main(String[] args) {
        String hostJar = "/Users/wangchao/eclipse-workspace/Host/target/Host-1.0.jar";
        GenericObjectSet genericObjectSet = new GenericObjectSet(hostJar);
//        System.out.println(ProjectInfo.i().getAllClassInfo().size());

//        genericObjectSet.generateObject("neu.lab.Host.Host");
//        for (ClassInfo c : ProjectInfo.i().getAllClassInfo()) {
//            System.out.println(c.getSig());
//        }
//
//        for (MethodInfo m : ProjectInfo.i().getAllMethod()) {
//            System.out.println(m.getSig());
//        }
        ClassInfo classInfo = ProjectInfo.i().getClassInfo("neu.lab.Host.A");
        if (classInfo == null) {
            return;
        }
        System.out.println(classInfo.getSig());
        ProjectInfo.i().setEntryCls("neu.lab.Host.A");
        List<MethodInfo> methodInfoList = classInfo.getAllConstructorContainsChildren();
        for (MethodInfo methodInfo : methodInfoList) {
            System.out.println(methodInfo.getSig());
        }
//        System.out.println(methodInfoList.size());
        //添加所有返回值为target class的方法
        for (MethodInfo methodInfo : ProjectInfo.i().getAllMethod()) {
            if (methodInfo.getReturnType().equals("neu.lab.Host.A")) {
                if (methodInfoList.contains(methodInfo)) {
                    continue;
                }
                methodInfoList.add(methodInfo);
            }
        }
        for (MethodInfo methodInfo : methodInfoList) {
            System.out.println(methodInfo.getSig());
        }

//        ClassInfo s = ProjectInfo.i().getClassInfo("");
//        System.out.println(s.getSig());
    }
}
