package cmssi.lyson.handler;

import java.util.Dictionary;

public class MappedWithDictionaryOfSubMapped {

	private String key1;
	
	private String key2;
			
	private Dictionary<String,SubMappedWithImplicitAndIdentityFst> key3;

	public MappedWithDictionaryOfSubMapped(){}
	
	public String getKey1() {
		return this.key1;
	}
	
	public String getKey2() {
		return this.key2;
	}
	
	public  Dictionary<String,SubMappedWithImplicitAndIdentityFst> getKey3() {
		return this.key3;
	}
}
