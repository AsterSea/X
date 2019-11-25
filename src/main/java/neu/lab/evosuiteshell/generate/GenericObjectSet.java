package neu.lab.evosuiteshell.generate;

import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.evosuiteshell.TestCaseUtil;
import neu.lab.evosuiteshell.search.*;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.NullAssertion;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClass;

import java.lang.reflect.Method;
import java.util.*;

public class GenericObjectSet {

    private InstrumentingClassLoader instrumentingClassLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();

    private TestCaseBuilder testCaseBuilder;

    public void generateObject(String targetClass) {
        ClassInfo targetClassInfo = ProjectInfo.i().getClassInfo(targetClass);
        if (targetClassInfo == null) {
            return;
        }
        ProjectInfo.i().setEntryCls(targetClass);
        //获取所有构造方法包括子类
        List<MethodInfo> methodInfoList = targetClassInfo.getAllConstructorContainsChildren();

        //添加所有返回值为target class的方法
        for (MethodInfo methodInfo : ProjectInfo.i().getAllMethod()) {
//            System.out.println(methodInfo.getSig() + " return : " + methodInfo.getReturnType());
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
            if (targetClassInfo.getAllConcreteType().contains(methodInfo.getCls().getSig())) {
                generate = generate(targetClassInfo, methodInfo);
            } else {
                String returnType = methodInfo.getReturnType();
                ClassInfo returnTypeClass = ProjectInfo.i().getClassInfo(returnType);
                if (returnTypeClass.getAllConcreteType().contains(targetClass)) {
                    generate = generateMethodCall(targetClassInfo, methodInfo);
                }
            }
            if (generate) {
                num++;
            }
        }

    }

    private boolean generateMethodCall(ClassInfo classInfo, MethodInfo methodInfo) {
        testCaseBuilder = new TestCaseBuilder();
        ClassInfo methodClass = methodInfo.getCls();
        MethodInfo bestConcreteForMethodClass = methodClass.getBestCons(false);
        List<NeededObj> neededParamsForMethodClass = new ArrayList<>();

        for (String paramType : bestConcreteForMethodClass.getParamTypes()) {
            neededParamsForMethodClass.add(new NeededObj(paramType, 0));

        }
        VariableReference variableReferenceForMethodClass;

        variableReferenceForMethodClass = structureParamTypes(testCaseBuilder, bestConcreteForMethodClass.getCls(), neededParamsForMethodClass);
        if (variableReferenceForMethodClass == null) {
            return false;
        }
        Method method;
        List<VariableReference> variableReferenceList = new ArrayList<VariableReference>();
        List<Class<?>> classList = new ArrayList<Class<?>>();
        try {
            Class<?> methodClazz = instrumentingClassLoader.loadClass(methodClass.getSig());
            List<NeededObj> neededObjList = new ArrayList<>();
            for (String paramType : methodInfo.getParamTypes()) {
                neededObjList.add(new NeededObj(paramType, 0));

            }

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
                            String paramString = SearchConstantPool.getInstance().getPoolValueRandom(classInfo.getSig().split("\\.")[classInfo.getSig().split("\\.").length - 1]);
                            if (paramString == null) {
                                paramString = Randomness.nextString(1);
                            }
                            variableReference = testCaseBuilder.appendStringPrimitive(paramString);
                            type = String.class;
                            break;
                    }
                    if (variableReference != null) {
                        variableReferenceList.add(variableReference);
                        classList.add(type);
                    }
                } else {//不是简单类型
                    try {
                        type = instrumentingClassLoader.loadClass(neededObj.getClassInfo().getSig());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        MavenUtil.i().getLog().error(e);
                    }
                    classList.add(type);
                    MethodInfo bestConcrete = neededObj.getClassInfo().getBestCons(false);
                    variableReferenceList.add(structureParamTypes(testCaseBuilder, neededObj.getClassInfo(), neededObj.getConsParamObs(bestConcrete)));
                }
            }
            method = methodClazz.getDeclaredMethod(methodInfo.getName(), classList.toArray(new Class[]{}));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        VariableReference variableReference = testCaseBuilder.appendMethod(variableReferenceForMethodClass, method, variableReferenceList.toArray(new VariableReference[]{}));

        return addSequenceToPool(variableReference, classInfo);
    }


    private boolean generate(ClassInfo classInfo, MethodInfo methodInfo) {
        testCaseBuilder = new TestCaseBuilder();
        List<NeededObj> neededParams = new ArrayList<>();
        for (String paramType : methodInfo.getParamTypes()) {
            neededParams.add(new NeededObj(paramType, 0));
        }
        VariableReference variableReference;
        if (classInfo.hasTargetChildren(methodInfo.getCls())) {
            variableReference = structureParamTypes(testCaseBuilder, methodInfo.getCls(), neededParams);
            // ？用classInfo 还是 methodInfo.getCls()
//            return addSequenceToPool(variableReference, methodInfo.getCls());
        } else {
            variableReference = structureParamTypes(testCaseBuilder, classInfo, neededParams);
//            return addSequenceToPool(variableReference, classInfo);
        }
        return addSequenceToPool(variableReference, classInfo);
    }

    private boolean addSequenceToPool(VariableReference variableReference, ClassInfo classInfo) {
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
//            System.out.println(objectPool.getNumberOfSequences());
            return true;
        }
    }


    private static GenericObjectSet instance = null;

    private GenericObjectSet() {
        //单例模式，只用soot解析一次host包内所有的classinfo和methodinfo
        String hostJarPath = DepJars.i().getHostDepJar().getJarFilePaths(true).toArray(new String[]{})[0];
        new SootExe().initProjectInfo(new String[]{hostJarPath});
    }

    public GenericObjectSet(String a) {
        //单例模式，只用soot解析一次host包内所有的classinfo和methodinfo
//        String hostJarPath = DepJars.i().getHostDepJar().getJarFilePaths(true).toArray(new String[]{})[0];
        new SootExe().initProjectInfo(new String[]{a});
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
                        String paramString = SearchConstantPool.getInstance().getPoolValueRandom(classInfo.getSig().split("\\.")[classInfo.getSig().split("\\.").length - 1]);
//                        variableReference = testCaseBuilder.appendStringPrimitive(ConstantPoolManager.getInstance().getConstantPool().getRandomString());
                        if (paramString == null) {
                            paramString = Randomness.nextString(1);
                        }
                        variableReference = testCaseBuilder.appendStringPrimitive(paramString);
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

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, ConstructionFailedException {
        HashSet<String> filesPath = TestCaseUtil.getFiles("/Users/wangchao/eclipse-workspace/Host/src/");
        for (String file : filesPath) {
            SearchPrimitiveManager.getInstance().search(file);
        }
        String cp = "/Users/wangchao/eclipse-workspace/Host/target/classes";
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
        Properties.CP = cp;
//        System.out.println("a.b.c.d".split("\\.")["a.b.c.d".split("\\.").length - 1]);
        String hostJar = "/Users/wangchao/eclipse-workspace/Host/target/Host-1.0.jar";
//        new SootExe().initProjectInfo(new String[]{hostJar});
//        System.out.println(ProjectInfo.i().getAllClassInfo().size());
        GenericObjectSet genericObjectSet = new GenericObjectSet(hostJar);
        genericObjectSet.generateObject("neu.lab.Host.A");
//        TestCase tc = ObjectPoolManager.getInstance().getRandomSequence(new GenericClass(genericObjectSet.instrumentingClassLoader.loadClass("neu.lab.Host.A")));
//        System.out.println(tc.toCode());
        for (TestCase testCase : ObjectPoolManager.getInstance().getSequences(new GenericClass(genericObjectSet.instrumentingClassLoader.loadClass("neu.lab.Host.A")))) {
            System.out.println(testCase.toCode());
        }
//        MethodStatement methodStatement = new MethodStatement()
//        System.out.println(tc.addStatement());
//        for (ClassInfo c : ProjectInfo.i().getAllClassInfo()) {
//            System.out.println(c.getSig());
//        }
//
//        for (MethodInfo m : ProjectInfo.i().getAllMethod()) {
//            System.out.println("1"+m.getSig());
//            System.out.println("2"+m.getName());
//        }
//        ClassInfo classInfo = ProjectInfo.i().getClassInfo("neu.lab.Host.A");
//        if (classInfo == null) {
//            return;
//        }
//        System.out.println(classInfo.getSig());
//        ProjectInfo.i().setEntryCls("neu.lab.Host.A");
//        List<MethodInfo> methodInfoList = classInfo.getAllConstructorContainsChildren();
//        for (MethodInfo methodInfo : methodInfoList) {
//            System.out.println(methodInfo.getSig());
//        }
////        System.out.println(methodInfoList.size());
//        //添加所有返回值为target class的方法
//        for (MethodInfo methodInfo : ProjectInfo.i().getAllMethod()) {
//            if (methodInfo.getReturnType().equals("neu.lab.Host.A")) {
//                if (methodInfoList.contains(methodInfo)) {
//                    continue;
//                }
//                methodInfoList.add(methodInfo);
//            }
//        }
//        for (MethodInfo methodInfo : methodInfoList) {
//            System.out.println(methodInfo.getSig());
//        }

//        ClassInfo s = ProjectInfo.i().getClassInfo("");
//        System.out.println(s.getSig());
    }
}
