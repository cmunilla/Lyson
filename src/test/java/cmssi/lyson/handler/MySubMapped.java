package cmssi.lyson.handler;

public class MySubMapped {

	private int subkey1;
	
	private MySubSubMapped subkey2;
			
	public MySubMapped(){}
	
	public int getSubKey1() {
		return this.subkey1;
	}
	
	public MySubSubMapped getSubKey2() {
		return this.subkey2;
	}
}
