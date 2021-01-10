package cmssi.lyson.handler;

import cmssi.lyson.annotation.LysonMapping;
import cmssi.lyson.handler.mapping.MappingConfiguration;

@LysonMapping(implicit=true)
public class SubMappedWithImplicitAndIdentityFst {

	@LysonMapping(mapping=MappingConfiguration.IDENTITY_MAPPING)
	private String name;
	
	private int subkey1;
	
	private SubMappedSnd subkey2;
			
	public SubMappedWithImplicitAndIdentityFst(){}
	
	public int getSubKey1() {
		return this.subkey1;
	}
	
	public SubMappedSnd getSubKey2() {
		return this.subkey2;
	}
	
	public String getName() {
		return this.name;
	}
}
