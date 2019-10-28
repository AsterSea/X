package neu.lab.evosuiteshell.generate;

import fj.Hash;
import neu.lab.conflict.vo.ClassVO;
import neu.lab.evosuiteshell.search.*;
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
import org.evosuite.utils.generic.GenericClass;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class GenericObjectSet {
    //    private GenericClass genericClass;
//    private Set<DefaultTestCase> defaultTestCaseSet;
//
//    public GenericObjectSet(Class<?> clazz) {
//        genericClass = new GenericClass(clazz);
//        defaultTestCaseSet = new HashSet<DefaultTestCase>();
//    }
    private static GenericObjectSet instance = null;

    private Map<GenericClass, Set<DefaultTestCase>> genericClassSetMap;

    private GenericObjectSet() {
        genericClassSetMap = new HashMap<GenericClass, Set<DefaultTestCase>>();
    }

    public static GenericObjectSet getInstance() {
        if (instance == null)
            instance = new GenericObjectSet();
        return instance;
    }

    public void addGenericObject(GenericClass genericClass, DefaultTestCase defaultTestCase) {
        Set<DefaultTestCase> defaultTestCases = genericClassSetMap.get(genericClass);
        if (defaultTestCases == null) {
            defaultTestCases = new HashSet<DefaultTestCase>();
            genericClassSetMap.put(genericClass, defaultTestCases);
        }
        defaultTestCases.add(defaultTestCase);
    }

    public void generateGenericObject(String classSig) {
        System.out.println(classSig);
//        TestCaseBuilder testCaseBuilder = new TestCaseBuilder();
//        ClassInfo classInfo = ProjectInfo.i().getClassInfo(classSig);
//        if (classInfo == null) {
//            return;
//        }
////        System.out.println(classSig + "asdf");
//        List<MethodInfo> methodInfoList = classInfo.getAllConstructor(false);
////        System.out.println(methodInfo.getSig());
////        List<String> paramTypes = methodInfo.getParamTypes();
//        List<NeededObj> neededParams = new ArrayList<NeededObj>();
//        for (MethodInfo methodInfo : methodInfoList) {
//            for (String paramType : methodInfo.getParamTypes()) {
//                neededParams.add(new NeededObj(paramType, 0));
//            }
//            EvosuiteNeedObject evosuiteNeedParamObject = structureParamTypes(classInfo, neededParams);
////            System.out.println(evosuiteNeedParamObject.toString());
//        }
//        VariableReference variableReference = structureParamTypes(testCaseBuilder, classInfo, neededParams);
//        ObjectPool objectPool = new ObjectPool();

//        TestCaseBuilder t = new TestCaseBuilder();

//        InstrumentingClassLoader instrumentingClassLoader=new InstrumentingClassLoader();
//        Class<?> clazzzzz=null;
//        try {
//            clazzzzz=instrumentingClassLoader.loadClassFromFile("neu.lab.Host.Host","/Users/wangchao/eclipse-workspace/Host/target/classes/neu/lab/Host/Host.class");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

//        objectPool.addSequence(variableReference.getGenericClass(), testCaseBuilder.getDefaultTestCase());
//        ObjectPoolManager.getInstance().addPool(objectPool);
//        System.out.println(testCaseBuilder.getDefaultTestCase().getStatement(1).getReturnValue().getType().getTypeName());


//        TestCase sequence = objectPool.getRandomSequence(new GenericClass(MyClassLoader.loaderClass(classInfo.getSig())));
//        System.out.println(sequence.toCode());
//        System.out.println("\n" + testCaseBuilder.toCode());
    }

    public EvosuiteNeedObject structureParamTypes(ClassInfo classInfo, List<NeededObj> neededObjList) {
//        EvosuiteNeedObject evosuiteNeedObject = new EvosuiteNeedObject(classInfo.getSig());
//        for (NeededObj neededObj : neededObjList) {
//            if (neededObj.isSimpleType()) {
//                evosuiteNeedParamObject.addEvosuiteNeedParamObject(new EvosuiteNeedObject(neededObj.getClassSig()));
//            } else {
//                MethodInfo methodInfo = neededObj.getClassInfo().getBestCons(false);
//                    evosuiteNeedParamObject.addEvosuiteNeedParamObject(structureParamTypes(neededObj.getClassInfo(), neededObj.getConsParamObs(methodInfo)));
//            }
//        }
//        return evosuiteNeedParamObject;
        return null;
    }

    //构建参数列表
    public VariableReference structureParamTypes(TestCaseBuilder testCaseBuilder, ClassInfo classInfo, List<NeededObj> neededObjList) {
        List<VariableReference> variableReferenceList = new ArrayList<VariableReference>();
        List<Class<?>> classList = new ArrayList<Class<?>>();
        for (NeededObj neededObj : neededObjList) {
//            System.out.println(neededObj.getClassSig());
            VariableReference variableReference = null;
//            Class<?> clazz = neededObj.getClassInfo().getClazz();
            classList.add(MyClassLoader.loaderClass(neededObj.getClassInfo().getSig()));
            if (neededObj.isSimpleType()) {
                switch (neededObj.getClassSig()) {
                    case "boolean":
                        variableReference = testCaseBuilder.appendBooleanPrimitive(true);
                        break;
                    case "byte":
                        variableReference = testCaseBuilder.appendBytePrimitive((byte) 1024);
                        break;
                    case "char":
                        variableReference = testCaseBuilder.appendCharPrimitive('c');
                        break;
                    case "short":
                        variableReference = testCaseBuilder.appendShortPrimitive((short) 0);
                        break;
                    case "int":
                        variableReference = testCaseBuilder.appendIntPrimitive(1);
                        break;
                    case "long":
                        variableReference = testCaseBuilder.appendLongPrimitive(1);
                        break;
                    case "float":
                        variableReference = testCaseBuilder.appendFloatPrimitive(1);
                        break;
                    case "double":
                        variableReference = testCaseBuilder.appendDoublePrimitive(1);
                        break;
                    case "java.lang.String":
                        variableReference = testCaseBuilder.appendStringPrimitive("Service-size");
                        break;
                }
                if (variableReference != null) {
                    variableReferenceList.add(variableReference);
                }
            } else {//不是简单类型
                MethodInfo bestConcrete = neededObj.getClassInfo().getBestCons(false);
                variableReferenceList.add(structureParamTypes(testCaseBuilder, neededObj.getClassInfo(), neededObj.getConsParamObs(bestConcrete)));
            }
        }
        VariableReference variableReferenceConstructor = null;
        try {
//            System.out.println(classList.toArray(new Class<?>[]{})[0]);
//            Class<?> classInfoSig = MyClassLoader.loaderClass(classInfo.getSig());
            InstrumentingClassLoader instrumentingClassLoader = new InstrumentingClassLoader();
            Class<?> clazz = instrumentingClassLoader.loadClass(classInfo.getSig());
            variableReferenceConstructor = testCaseBuilder.appendConstructor(clazz.getConstructor(classList.toArray(new Class<?>[]{})), variableReferenceList.toArray(new VariableReference[]{}));
        } catch (Exception e) {

        }
        return variableReferenceConstructor;
//        return variableReferenceList;
    }

    public void reset() {
        genericClassSetMap.clear();
        GenericObjectSet.instance = null;
    }

    public static void main(String[] args) throws ClassNotFoundException {
//        String cp = System.getProperty("user.dir") + "/target/classes";
//        String cp = "/Users/wangchao/eclipse-workspace/Host/target/classes/";
//        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
//        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(System.getProperty("user.dir") + "/target/test-classes");
        Properties.TARGET_CLASS = "neu.lab.Host.Host";
        Properties.CP = "/Users/wangchao/eclipse-workspace/Host/target/classes/:/Users/wangchao/.m2/repository/neu/lab/A/1.0/A-1.0.jar:/Users/wangchao/.m2/repository/neu/lab/B/2.0/B-2.0.jar:/Users/wangchao/eclipse-workspace/Host/target/test-classes/";
        Properties.SELECTED_JUNIT = "neu.lab.Host.HostTest";
        InstrumentingClassLoader instrumentingClassLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();
        final org.evosuite.testcarver.extraction.CarvingClassLoader classLoader = new org.evosuite.testcarver.extraction.CarvingClassLoader();
        Class<?> Host = instrumentingClassLoader.loadClass("neu.lab.Host.Host");
        Class<?> Hosttest = instrumentingClassLoader.loadClass("neu.lab.Host.HostTest");

//        ObjectPool.getPoolFromJUnit(new GenericClass(Host), Hosttest);
        //        Class<?> clazz = instrumentingClassLoader.loadClass(neu.lab.evosuiteshell.Config.class.getCanonicalName());
//        System.out.println(clazz);
//        final org.evosuite.testcarver.extraction.CarvingClassLoader classLoader = new org.evosuite.testcarver.extraction.CarvingClassLoader();
//        Class host = neu.lab.evosuiteshell.junit.ExecuteJunit.class;//classLoader.loadClass("neu.lab.evosuiteshell.junit.ExecuteJunit");
//        Class hostTest = neu.lab.evosuiteshell.junit.ExecuteJunit;
//            ObjectPool.getPoolFromJUnit(new GenericClass(host),hostTest);
    }
}
