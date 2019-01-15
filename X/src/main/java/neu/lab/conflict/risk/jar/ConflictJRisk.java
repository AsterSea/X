package neu.lab.conflict.risk.jar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import neu.lab.conflict.container.AllCls;
import neu.lab.conflict.container.AllRefedCls;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.graph.Dog;
import neu.lab.conflict.graph.Graph4distance;
import neu.lab.conflict.graph.IBook;
import neu.lab.conflict.graph.Dog.Strategy;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.Conflict;
import neu.lab.conflict.vo.DepJar;

/**
 * 有风险的冲突jar
 * 
 * @author wangchao
 *
 */
public class ConflictJRisk {

	private Conflict conflict; // 冲突
	private List<DepJarJRisk> jarRisks; // 依赖风险jar集合

	/*
	 * 构造函数
	 */
	public ConflictJRisk(Conflict conflict) {
		this.conflict = conflict;
		
	}

	}