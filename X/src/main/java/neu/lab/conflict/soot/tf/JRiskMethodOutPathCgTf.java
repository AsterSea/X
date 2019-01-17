package neu.lab.conflict.soot.tf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import neu.lab.conflict.graph.Graph4path;
import neu.lab.conflict.graph.GraphForMethodOutPath;
import neu.lab.conflict.graph.Node4path;
import neu.lab.conflict.risk.jar.DepJarJRisk;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.vo.MethodCall;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class JRiskMethodOutPathCgTf extends JRiskCgTf{

	public JRiskMethodOutPathCgTf(DepJarJRisk depJarJRisk) {
		super(depJarJRisk);
	}
	public JRiskMethodOutPathCgTf(Set<String> entryMethods) {
		super(entryMethods);
	}
	
	@Override
	protected void formGraph() {
		if (graph == null) {
			MavenUtil.i().getLog().info("start form graph...");
			// get call-graph.
			
			Map<String, List<String>> methodsOutPath = new HashMap<String, List<String>>();
			
			CallGraph cg = Scene.v().getCallGraph();
			
			Iterator<Edge> ite = cg.iterator();
			
			while (ite.hasNext()) {
				Edge edge = ite.next();
//				System.out.println("Edge " + edge.toString());
				if (edge.src().isJavaLibraryMethod() || !edge.src().isConcrete()) {
				} 
				else {
					String srcMethodName = edge.src().getSignature();
//					System.out.println("before riskMthds" + srcMethodName);
					if (riskMthds.contains(srcMethodName)) {
//						System.out.println("riskMthds" + srcMethodName);
						List<String> outMethodPath = methodsOutPath.get(srcMethodName);
						if(outMethodPath == null) {
							outMethodPath = new ArrayList<String>();
							methodsOutPath.put(srcMethodName, outMethodPath);
						}
						String tgtMthdName = edge.tgt().getSignature();
						if (outMethodPath.contains(tgtMthdName)) {
						}else {
							outMethodPath.add(tgtMthdName);
						}
						//outMethodPath.add(tgtMthdName);
					}
				}
				
//				String srcMthdName = edge.src().getSignature();
//				String tgtMthdName = edge.tgt().getSignature();
//				String srcClsName = edge.src().getDeclaringClass().getName();
//				String tgtClsName = edge.tgt().getDeclaringClass().getName();
//				if (edge.src().isJavaLibraryMethod() || edge.tgt().isJavaLibraryMethod()) {
//				} else {
//					if (edge.src().isConcrete() || edge.tgt().isConcrete()) {
//					if (!name2node.containsKey(srcMthdName)) {
//						name2node.put(srcMthdName, new Node4path(srcMthdName, isHostClass(srcClsName)&&!edge.src().isPrivate(),
//								riskMthds.contains(srcMthdName)));
//					}
//					if (!name2node.containsKey(tgtMthdName)) {
//						name2node.put(tgtMthdName, new Node4path(tgtMthdName, isHostClass(tgtClsName)&&!edge.tgt().isPrivate(),
//								riskMthds.contains(tgtMthdName)));
//					}
//					mthdRlts.add(new MethodCall(srcMthdName, tgtMthdName));
//				}}
			}
			graph = new GraphForMethodOutPath(methodsOutPath);
			MavenUtil.i().getLog().info("end form graph.");
		}
	}

	@Override
	protected void initMthd2branch() {
		
	}

}
