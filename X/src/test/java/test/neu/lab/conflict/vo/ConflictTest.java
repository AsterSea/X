package test.neu.lab.conflict.vo;

import neu.lab.conflict.vo.Conflict;

public class ConflictTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Conflict conflict = new Conflict("a", "b");
		System.out.println(conflict.getUsedDepJar());
	}

}
