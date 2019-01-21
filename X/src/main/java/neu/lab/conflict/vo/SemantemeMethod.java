package neu.lab.conflict.vo;

public class SemantemeMethod {

	String methodName;	//方法名
	int intersection;	//两个版本同名方法的call graph的out路径的交集
	int difference;		//两个版本同名方法的call graph的out路径的差集
//	String depJarVersion;	//父类版本
//	boolean referenceChange;	//是否引用被改变，如果因为版本屏蔽而指向了新版本中的本方法则为true
	boolean thrownMethod;	//是不是thrown方法，会抛出
	
	
	
	public SemantemeMethod(String methodName, int intersection, int difference, boolean thrownMethod) {
		super();
		this.methodName = methodName;
		this.intersection = intersection;
		this.difference = difference;
		this.thrownMethod = thrownMethod;
	}
	public SemantemeMethod(String methodName) {
		super();
		this.methodName = methodName;
	}
	public boolean isThrownMethod() {
		return thrownMethod;
	}
	public void setThrownMethod(boolean thrownMethod) {
		this.thrownMethod = thrownMethod;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public int getIntersection() {
		return intersection;
	}
	public void setIntersection(int intersection) {
		this.intersection = intersection;
	}
	public int getDifference() {
		return difference;
	}
	public void setDifference(int difference) {
		this.difference = difference;
	}
//	public String getDepJarVersion() {
//		return depJarVersion;
//	}
//	public void setDepJarVersion(String depJarVersion) {
//		this.depJarVersion = depJarVersion;
//	}
//	public boolean isReferenceChange() {
//		return referenceChange;
//	}
//	public void setReferenceChange(boolean referenceChange) {
//		this.referenceChange = referenceChange;
//	}
	
}
