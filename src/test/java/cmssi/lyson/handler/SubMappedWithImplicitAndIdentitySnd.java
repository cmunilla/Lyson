package cmssi.lyson.handler;

import cmssi.lyson.annotation.LysonMapping;
import cmssi.lyson.handler.mapping.MappingConfiguration;

@LysonMapping(implicit=true)
public class SubMappedWithImplicitAndIdentitySnd {

	@LysonMapping(mapping=MappingConfiguration.IDENTITY_MAPPING)
	private String name;
	
	private int subkey1;
	
	private SubMappedWithImplicitAndIdentityThd subkey2;
			
	public SubMappedWithImplicitAndIdentitySnd(){}
	
	public int getSubKey1() {
		return this.subkey1;
	}
	
	public SubMappedWithImplicitAndIdentityThd getSubKey2() {
		return this.subkey2;
	}
	
	public String getName() {
		return this.name;
	}
}
