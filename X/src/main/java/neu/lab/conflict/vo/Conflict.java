package neu.lab.conflict.vo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import neu.lab.conflict.risk.jar.ConflictJRisk;

public class Conflict {
	private String groupId;
	private String artifactId;
	/*
	 * 一个conflict中存在n个depJar n>1 一个depJar对应1:n个node
	 */
	private Set<NodeAdapter> nodeAdapters;
	private Set<DepJar> depJars;
	private DepJar usedDepJar;
	// private ConflictRiskAna riskAna;

	public Conflict(String groupId, String artifactId) {
		nodeAdapters = new HashSet<NodeAdapter>();
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	/**
	 * 得到使用的DepJar
	 * 
	 * @return
	 */
	public DepJar getUsedDepJar() {
		if (null == usedDepJar) {
			for (DepJar depJar : depJars) {
				if (depJar.isSelected()) {
					usedDepJar = depJar;
				}
			}
		}
		return usedDepJar;

	}

	/**
	 * 设置usedDepJar
	 */
	public void setUsedDepJar(DepJar depJar) {
		usedDepJar = depJar;
	}

	/**
	 * 得到除了被选中的jar以外的其他被依赖的jar包
	 * 
	 * @return
	 */
	public Set<DepJar> getOtherDepJarExceptSelect() {
		Set<DepJar> usedDepJars = new HashSet<DepJar>();
		for (DepJar depJar : depJars) {
			if (!depJar.isSelected()) {
				usedDepJars.add(depJar);
			}
		}
		return usedDepJars;
	}

	public void addNodeAdapter(NodeAdapter nodeAdapter) {
		nodeAdapters.add(nodeAdapter);
	}

	/**
	 * 同一个构件
	 * 
	 * @param groupId2
	 * @param artifactId2
	 * @return
	 */
	public boolean sameArtifact(String groupId2, String artifactId2) {
		return groupId.equals(groupId2) && artifactId.equals(artifactId2);
	}

	public Set<DepJar> getDepJars() {
		if (depJars == null) {
			depJars = new HashSet<DepJar>();
			for (NodeAdapter nodeAdapter : nodeAdapters) {
				depJars.add(nodeAdapter.getDepJar());
			}
		}
		return depJars;
	}

	public Set<NodeAdapter> getNodeAdapters() {
		return this.nodeAdapters;
	}

	public boolean isConflict() {
		return getDepJars().size() > 1;
	}

	public ConflictJRisk getJRisk() {
		return new ConflictJRisk(this);
	}

	@Override
	public String toString() {
		String str = groupId + ":" + artifactId + " conflict version:";
		for (DepJar depJar : depJars) {
			str = str + depJar.getVersion() + ":" + depJar.getClassifier() + "-";
		}
		str = str + "---used jar:" + getUsedDepJar().getVersion() + ":" + getUsedDepJar().getClassifier();
		return str;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getSig() {
		return getGroupId() + ":" + getArtifactId();
	}

	/**
	 * @return first version is the used version 第一个版本是正在使用的版本
	 */
	public List<String> getVersions() {
		List<String> versions = new ArrayList<String>();
		versions.add(getUsedDepJar().getVersion());
		for (DepJar depJar : depJars) {
			String version = depJar.getVersion();
			if (!versions.contains(version)) {
//				versions.add("/" + version);
				versions.add(version);
			}
		}
		return versions;
	}
}
