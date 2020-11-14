package cmssi.lyson.handler;

import cmssi.lyson.annotation.LysonMapping;

@LysonMapping(implicit=true)
public class IdentitySubMapped {

	@LysonMapping(mapping=MappingConfiguration.IDENTITY_MAPPING)
	private String name;
	
	private int subkey1;
	
	private IdentitySubSubMapped subkey2;
			
	public IdentitySubMapped(){}
	
	public int getSubKey1() {
		return this.subkey1;
	}
	
	public IdentitySubSubMapped getSubKey2() {
		return this.subkey2;
	}
	
	public String getName() {
		return this.name;
	}
}
