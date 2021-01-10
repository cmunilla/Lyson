package cmssi.lyson.handler;

import java.util.List;

public class MappedWithListOfSubMapped {

	private String key1;
	
	private String key2;
			
	private List<SubMapped> key3;

	public MappedWithListOfSubMapped(){}
	
	public String getKey1() {
		return this.key1;
	}
	
	public String getKey2() {
		return this.key2;
	}
	
	public List<SubMapped> getKey3() {
		return this.key3;
	}
}
