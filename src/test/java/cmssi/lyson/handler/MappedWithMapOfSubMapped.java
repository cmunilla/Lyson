package cmssi.lyson.handler;

import java.util.Map;

public class MappedWithMapOfSubMapped {

	private String key1;
	
	private String key2;
			
	private Map<String,SubMappedWithImplicitAndIdentityFst> key3;

	public MappedWithMapOfSubMapped(){}
	
	public String getKey1() {
		return this.key1;
	}
	
	public String getKey2() {
		return this.key2;
	}
	
	public  Map<String,SubMappedWithImplicitAndIdentityFst> getKey3() {
		return this.key3;
	}
}
