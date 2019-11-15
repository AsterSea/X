package neu.lab.evosuiteshell.generate;

import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.evosuiteshell.search.*;
import org.evosuite.TestGenerationContext;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClass;

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
        System.out.println(1);
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
            System.out.println(methodInfo.getSig());
            boolean generate = false;
            if (num > 10) break;

            if (methodInfo.getCls().getSig().equals(targetClass)) {
                generate = generate(targetClassInfo, methodInfo);
            } else if (methodInfo.getReturnType().equals(targetClass)) {
//TODO 返回值为我们所需要的类，如何构造这个方法所在的类
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
                e.printStackTrace();
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

    public static GenericObjectSet getInstance() {
        if (instance == null)
            instance = new GenericObjectSet();
        return instance;
    }


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
                        String patamString = SearchConstantPool.getInstance().getPoolValueRandom(classInfo.getSig().split("\\.")[classInfo.getSig().split("\\.").length - 1]);
//                        variableReference = testCaseBuilder.appendStringPrimitive(ConstantPoolManager.getInstance().getConstantPool().getRandomString());
                        variableReference = testCaseBuilder.appendStringPrimitive(patamString);
//                        variableReference = testCaseBuilder.appendStringPrimitive("AWS-size");
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
                    e.printStackTrace();
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
            e.printStackTrace();
            MavenUtil.i().getLog().error(e);
            return null;
        }

        return variableReferenceConstructor;
    }

    public static void main(String[] args) {
        System.out.println("a.b.c.d".split("\\.")["a.b.c.d".split("\\.").length - 1]);
        String hostJar = "/Users/wangchao/eclipse-workspace/Host/target/Host-1.0.jar";
        new SootExe().initProjectInfo(new String[]{hostJar});
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
