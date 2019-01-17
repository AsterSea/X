package neu.lab.conflict.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import neu.lab.conflict.vo.MethodCall;

public class GraphForMethodOutPath implements IGraph{
	
	Map<String,List<String>> methodOutPath;
	public GraphForMethodOutPath(Map<String,List<String>> methodOutPath) {
		this.methodOutPath = methodOutPath;
	}

public Set<String> comparedMethodOutPath(Map<String,List<String>> entryMehtodOutPath){
	HashSet<String> differenceMethod = new HashSet<String>();
	for (String method : methodOutPath.keySet()) {
		if (differenceMethod.size() > 100) {
			break;
		}
		List<String> entryOutPath = entryMehtodOutPath.get(method);
		List<String> thisOutPath = methodOutPath.get(method);
		if (entryOutPath == null && thisOutPath == null) {
		}
		else if (entryOutPath == null || thisOutPath == null) {
			differenceMethod.add(method);
		}else {
			if (entryOutPath.size() <= 12 || thisOutPath.size() <= 12) {
				continue;
			}
			if (thisOutPath.size() != entryOutPath.size()) {
				differenceMethod.add(method);
			}else {
				for (String outMethod : entryOutPath) {
					if (thisOutPath.contains(outMethod)) {
					}else {
						differenceMethod.add(method);
						break;
					}
				}
			}
		}
//		System.out.println("thisOutPath" + thisOutPath.size() + ">>>>>" + "entryOutPath" + entryOutPath.size());
	}
	return differenceMethod;
}

@Override
public INode getNode(String nodeName) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Collection<String> getAllNode() {
	// TODO Auto-generated method stub
	return null;
}
public Map<String,List<String>> getMethodOutPath(){
	return methodOutPath;
}
}
