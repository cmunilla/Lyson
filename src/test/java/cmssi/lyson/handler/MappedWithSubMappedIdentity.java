package cmssi.lyson.handler;

public class MappedWithSubMappedIdentity {

	private String key1;
	
	private String key2;
			
	private SubMappedWithImplicitAndIdentitySnd key3;

	public MappedWithSubMappedIdentity(){}
	
	public String getKey1() {
		return this.key1;
	}
	
	public String getKey2() {
		return this.key2;
	}
	
	public SubMappedWithImplicitAndIdentitySnd getKey3() {
		return this.key3;
	}
}
