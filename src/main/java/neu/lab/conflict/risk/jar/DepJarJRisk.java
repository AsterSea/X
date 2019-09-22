package neu.lab.conflict.risk.jar;

import java.io.File;
import java.util.*;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import neu.lab.conflict.GlobalVar;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.container.SemantemeMethods;
import neu.lab.conflict.graph.Graph4distance;
import neu.lab.conflict.graph.Graph4path;
import neu.lab.conflict.graph.GraphForMethodOutPath;
import neu.lab.conflict.graph.IBook;
import neu.lab.conflict.graph.IGraph;
import neu.lab.conflict.graph.IRecord;
import neu.lab.conflict.graph.Node4distance;
import neu.lab.conflict.graph.Node4path;
import neu.lab.conflict.graph.Record4distance;
import neu.lab.conflict.graph.Record4path;
import neu.lab.conflict.soot.SootJRiskCg;
import neu.lab.conflict.soot.SootRiskMthdFilter;
import neu.lab.conflict.soot.SootRiskMthdFilter2;
import neu.lab.conflict.soot.tf.JRiskDistanceCgTf;
import neu.lab.conflict.soot.tf.JRiskMethodOutPathCgTf;
import neu.lab.conflict.soot.tf.JRiskMthdPathCgTf;
import neu.lab.conflict.util.*;
import neu.lab.conflict.vo.DepJar;
import neu.lab.conflict.vo.MethodCall;
import neu.lab.evosuiteshell.Config;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.printer.Printer;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

/**
 * 依赖风险jar
 *
 * @author wangchao
 */
public class DepJarJRisk {
    private DepJar depJar; // 依赖jar
    private DepJar usedDepJar; // 依赖jar
    private Set<String> thrownMthds; // 抛弃的方法
    private Set<String> semantemeRiskMethods; // 语义风险方法集合
    // private Set<String> rchedMthds;
    private Graph4distance graph4distance; // 图
//	private Map<String, IBook> books; // book记录用

    /*
     * 构造函数
     */
    public DepJarJRisk(DepJar depJar, DepJar usedDepJar) {
        this.depJar = depJar;
        this.usedDepJar = usedDepJar;
        // calculate thrownMthd
        // calculate call-graph
    }

    /*
     * 得到版本
     */
    public String getVersion() {
        return depJar.getVersion();
    }

    public DepJar getUsedDepJar() {
        return usedDepJar;
    }

    public void setUsedDepJar(DepJar usedDepJar) {
        this.usedDepJar = usedDepJar;
    }

    public Set<String> getThrownClasses() {
        Set<String> thrownClasses = usedDepJar.getRiskClasses(depJar.getAllCls(false));
        return thrownClasses;
    }

    /**
     * 得到抛弃的方法
     *
     * @return
     */
    public Set<String> getThrownMthds() {
        // e.g.:"<neu.lab.plug.testcase.homemade.host.prob.ProbBottom: void m()>"
        thrownMthds = usedDepJar.getRiskMthds(depJar.getallMethods());
        MavenUtil.i().getLog().info("riskMethod size before filter: " + thrownMthds.size());
        if (thrownMthds.size() > 0)
            new SootRiskMthdFilter().filterRiskMthds(thrownMthds);
        MavenUtil.i().getLog().info("riskMethod size after filter1: " + thrownMthds.size());
        if (thrownMthds.size() > 0)
            new SootRiskMthdFilter2().filterRiskMthds(this, thrownMthds);
        MavenUtil.i().getLog().info("riskMethod size after filter2: " + thrownMthds.size());
        return thrownMthds;
    }

    /**
     * 用传入的depJar去得到抛弃的方法
     *
     * @param enterDepJar
     * @return
     */
    public Set<String> getThrownMthds(DepJar enterDepJar) {
        thrownMthds = usedDepJar.getRiskMthds(depJar.getallMethods());
        MavenUtil.i().getLog().info("riskMethod size before filter: " + thrownMthds.size());
        if (thrownMthds.size() > 0)
            new SootRiskMthdFilter().filterRiskMthds(thrownMthds, enterDepJar);
        MavenUtil.i().getLog().info("riskMethod size after filter1: " + thrownMthds.size());
        if (thrownMthds.size() > 0)
            new SootRiskMthdFilter2().filterRiskMthds(this, thrownMthds);
        MavenUtil.i().getLog().info("riskMethod size after filter2: " + thrownMthds.size());
        return thrownMthds;
    }

    /**
     * 语义冲突得到相关方法
     *
     * @return
     */
    public Set<String> getSemantemeRiskMethods() {
        semantemeRiskMethods = usedDepJar.getCommonMethods(depJar.getallMethods());
        MavenUtil.i().getLog().info("semantemeRiskMethods size for common methods: " + semantemeRiskMethods.size());
        return semantemeRiskMethods;
    }

    public Set<String> getMethodBottom(Map<String, IBook> books) {
        Set<String> bottomMethods = new HashSet<String>();
        for (IBook book : books.values()) {
            for (IRecord iRecord : book.getRecords()) {
                Record4distance record = (Record4distance) iRecord;
                bottomMethods.add(record.getName());
            }
        }
        return bottomMethods;
    }

    public Set<String> getMethodBottomForPath(Map<String, IBook> books) {
        Set<String> bottomMethods = new HashSet<String>();
        for (IBook book : books.values()) {
            for (IRecord iRecord : book.getRecords()) {
                Record4path record = (Record4path) iRecord;
                bottomMethods.add(record.getName());
            }
        }
        return bottomMethods;
    }

    public Collection<String> getPrcDirPaths() throws Exception {
        List<String> classpaths;
        if (GlobalVar.useAllJar) { // default:true
            classpaths = depJar.getRepalceClassPath();
        } else {
            MavenUtil.i().getLog().info("not add all jar to process");
            classpaths = new ArrayList<String>();
            // keep first is self
            classpaths.addAll(this.depJar.getJarFilePaths(true));
            classpaths.addAll(this.depJar.getFatherJarClassPaths(false));

        }
        return classpaths;

    }

    public DepJar getEntryDepJar() {
        return DepJars.i().getHostDepJar();
    }

    public DepJar getConflictDepJar() {
        return depJar;
    }

    /**
     * 得到距离图
     *
     * @return
     */
    public Graph4distance getGraph4distance() {
        if (graph4distance == null) {
            Set<String> thrownmethods = getThrownMthds();
            if (thrownmethods.size() > 0) {
                IGraph iGraph = SootJRiskCg.i().getGraph(this, new JRiskDistanceCgTf(this, thrownmethods));
                if (iGraph != null) {
                    graph4distance = (Graph4distance) iGraph;
                } else {
                    graph4distance = new Graph4distance(new HashMap<String, Node4distance>(),
                            new ArrayList<MethodCall>());
                }
            } else {
                graph4distance = new Graph4distance(new HashMap<String, Node4distance>(), new ArrayList<MethodCall>());
            }
        }
        return graph4distance;
    }

    /**
     * 得到距离图 多态
     *
     * @return
     */
    public Graph4distance getGraph4distance(DepJar useDepJar) {
        Set<String> thrownmethods = getThrownMthds(useDepJar);
        if (thrownmethods.size() > 0) {
            IGraph iGraph = SootJRiskCg.i().getGraph(this, new JRiskDistanceCgTf(this, thrownmethods));
            if (iGraph != null) {
                return (Graph4distance) iGraph;
            } else {
                return new Graph4distance(new HashMap<String, Node4distance>(), new ArrayList<MethodCall>());
            }
        } else {
            return new Graph4distance(new HashMap<String, Node4distance>(), new ArrayList<MethodCall>());
        }
    }

    public Graph4path getGraph4mthdPath() {
        Set<String> semantemeRiskMethods = getSemantemeRiskMethods();
        if (semantemeRiskMethods.size() > 0) {
            IGraph iGraph = SootJRiskCg.i().getGraph(this, new JRiskMthdPathCgTf(this, semantemeRiskMethods));
            if (iGraph != null) {
                return (Graph4path) iGraph;
            } else {
                return new Graph4path(new HashMap<String, Node4path>(), new ArrayList<MethodCall>());
            }
        } else {
            return new Graph4path(new HashMap<String, Node4path>(), new ArrayList<MethodCall>());
        }
//		if (getThrownMthds().size() > 0) {
//			IGraph iGraph = SootJRiskCg.i().getGraph4branch(this,new JRiskMthdPathCgTf(this));
//			if(iGraph!=null)
//				return (Graph4path)iGraph;
//		}
//		return new Graph4path(new HashMap<String, Node4path>(), new ArrayList<MethodCall>());
    }

    Map<String, List<Integer>> semantemeMethodForDifferences; // 语义方法的差异集合

    public Map<String, List<Integer>> getSemantemeMethodForDifferences() {
        return semantemeMethodForDifferences;
    }

    public void getAllSemantemeMethodForDifferences() {

        Set<String> semantemeRiskMethods = getSemantemeRiskMethods();
//		Set<String> riskMethods = new HashSet<String>();
        if (semantemeRiskMethods.size() > 0) {
            GraphForMethodOutPath depJarGraphForMethodOutPath = (GraphForMethodOutPath) SootJRiskCg.i().getGraph(depJar,
                    new JRiskMethodOutPathCgTf(semantemeRiskMethods));

            GraphForMethodOutPath usedDepJarGraphForMethodOutPath = (GraphForMethodOutPath) SootJRiskCg.i()
                    .getGraph(usedDepJar, new JRiskMethodOutPathCgTf(semantemeRiskMethods));

            SemantemeMethods semantemeMethods = new SemantemeMethods(depJarGraphForMethodOutPath.getSemantemeMethods(),
                    usedDepJarGraphForMethodOutPath.getSemantemeMethods());

            semantemeMethods.CalculationDifference(); // 计算差异

            semantemeMethodForDifferences = semantemeMethods.getSemantemeMethodForReturn();
        }
    }

    // depJar 的反编译文件路径
    private String depJarDecompressionPath;
    // used depJar 的反编译文件路径
    private String usedDepJarDecompressionPath;

    // 得到语义冲突的路径图
    public Graph4distance getMethodPathGraphForSemanteme() {

        Set<String> semantemeRiskMethods = getSemantemeRiskMethods();
        Set<String> riskMethods = new HashSet<String>();

        if (semantemeRiskMethods.size() > 0) {

            decompile();

            riskMethods = calculationDifference(semantemeRiskMethods);

            if (riskMethods.size() > 0) {
                IGraph iGraph = SootJRiskCg.i().getGraph(this, new JRiskDistanceCgTf(this, riskMethods));
                if (iGraph != null) {
                    return (Graph4distance) iGraph;
                } else {
                    return new Graph4distance(new HashMap<String, Node4distance>(), new ArrayList<MethodCall>());
                }
            } else {
                return new Graph4distance(new HashMap<String, Node4distance>(), new ArrayList<MethodCall>());
            }
        } else {
            return new Graph4distance(new HashMap<String, Node4distance>(), new ArrayList<MethodCall>());
        }
    }

    /**
     * 解压冲突jar包
     */
    public void decompile() {

        depJarDecompressionPath = JARDecompressionTool.decompressionPath + depJar.getDepJarName() + Config.FILE_SEPARATOR;
        usedDepJarDecompressionPath = JARDecompressionTool.decompressionPath + usedDepJar.getDepJarName() + Config.FILE_SEPARATOR;

        JARDecompressionTool.decompress(depJar.getJarFilePaths(true).get(0), depJarDecompressionPath);

        JARDecompressionTool.decompress(usedDepJar.getJarFilePaths(true).get(0), usedDepJarDecompressionPath);
    }

    /**
     * 计算共有方法对的ast tree的差异集合
     *
     * @param semantemeRiskMethods 两个jar包共有的方法集合，即有可能存在语义冲突的方法集合
     * @return 返回有差异的方法对，默认返回排序后的前100个
     */
    private Set<String> calculationDifference(Set<String> semantemeRiskMethods) {

        Map<String, Integer> semantemeMethodForDifferences = new HashMap<String, Integer>(); // 语义方法的差异集合

        Loader loaderDepJar = new JDCoreLoader(new File(depJarDecompressionPath));
        Loader loaderUsedDepJar = new JDCoreLoader(new File(usedDepJarDecompressionPath));
        Printer printerDepJar = new JDCorePrinter();
        Printer printerUsedDepJar = new JDCorePrinter();

        ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();

        AstComparator astComparator = new AstComparator();

        // key class
        // value methods
        Map<String, Set<String>> methodsFromClass = new HashMap<String, Set<String>>();
        try {
            for (String method : semantemeRiskMethods) {
                Set<String> methods = methodsFromClass.get(SootUtil.mthdSig2cls(method));
                if (methods == null) {
                    methods = new HashSet<>();
                }
                methods.add(method);
                methodsFromClass.put(SootUtil.mthdSig2cls(method), methods);
            }

            for (String methodClassSig : methodsFromClass.keySet()) {

                decompiler.decompile(loaderDepJar, printerDepJar, methodClassSig.replace(".", File.separator));
                decompiler.decompile(loaderUsedDepJar, printerUsedDepJar, methodClassSig.replace(".", File.separator));
                MavenUtil.i().getLog().info("decompiler success");

                for (String method : methodsFromClass.get(methodClassSig)) {

                    String depJarContent = printerDepJar.toString();
                    String usedDepJarContent = printerUsedDepJar.toString();

                    try {
                        List<CtMethod<?>> depJarCtMethods = astComparator.getCtType(depJarContent).getMethodsByName(SootUtil.mthdSig2methodName(method));

                        List<CtMethod<?>> usedDepJarCtMethods = astComparator.getCtType(usedDepJarContent).getMethodsByName(SootUtil.mthdSig2methodName(method));

                        CtMethod ctMethodFromDepJar = getCtMethod(depJarCtMethods, SootUtil.mthdSig2param(method));

                        CtMethod ctMethodFromUsedDepJar = getCtMethod(usedDepJarCtMethods, SootUtil.mthdSig2param(method));

                        if (ctMethodFromDepJar == null || ctMethodFromUsedDepJar == null) {
                            continue;
                        }
                        Diff diff = astComparator.compare(ctMethodFromDepJar, ctMethodFromUsedDepJar);

                        int differentSize = diff.getRootOperations().size();

                        semantemeMethodForDifferences.put(method, differentSize);

                    } catch (Exception e) {
                        MavenUtil.i().getLog().error(e.toString() + " method : " + method);
                    }
                }
            }

        } catch (Exception e) {
            MavenUtil.i().getLog().error(e.toString());
            e.printStackTrace();
        }

        return sortMap(semantemeMethodForDifferences, 100);
    }

    /**
     * 通过输入的参数列表找到特定的方法
     *
     * @param ctMethods
     * @param sootMethodParams
     * @return
     */
    private CtMethod<?> getCtMethod(List<CtMethod<?>> ctMethods, String sootMethodParams) {

        if (sootMethodParams.length() == 0) {
            for (CtMethod<?> ctMethod : ctMethods) {
                if (ctMethod.getParameters().size() == 0) {
                    return ctMethod;
                }
            }
        } else if (!sootMethodParams.contains(",")) {
            for (CtMethod<?> ctMethod : ctMethods) {
                if (ctMethod.getParameters().size() == 1 && (ctMethod.getParameters().get(0)).getType().getQualifiedName().equals(sootMethodParams)) {
                    return ctMethod;
                }
            }
        } else {
            String[] params = sootMethodParams.split(",");
            for (CtMethod<?> ctMethod : ctMethods) {
                int flag = 0;
                if (ctMethod.getParameters().size() == params.length) {
                    for (int i = 0; i < params.length; i++) {
                        if (ctMethod.getParameters().get(i).getType().getQualifiedName().equals(params[i])) {
                            flag++;
                        } else {
                            break;
                        }
                    }
                    if (flag == params.length) {
                        return ctMethod;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 对Map排序后，输出前N个Intger最大的method 降序
     *
     * @param entrySize 大小限制，输出多少个排序后数组
     * @return
     */
    private Set<String> sortMap(Map<String, Integer> semantemeMethodForDifferences, int entrySize) {
        if (semantemeMethodForDifferences.size() == 0) {
            return null;
        }
        Set<String> afterSortMethods = new HashSet<String>();
        List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(
                semantemeMethodForDifferences.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
                return obj2.getValue() - obj1.getValue();
            }
        });
        int size = 0;
        if (semantemeMethodForDifferences.size() > entrySize) {
            size = entrySize;
        } else {
            size = semantemeMethodForDifferences.size();
        }
        for (int i = 0; i < size; i++) {
            afterSortMethods.add(entries.get(i).getKey());
        }
        return afterSortMethods;
    }

    @Override
    public String toString() {
        return depJar.toString() + " in conflict " + usedDepJar.toString();
    }

}
