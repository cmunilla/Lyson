package cmssi.lyson.handler;

import cmssi.lyson.annotation.LysonMapping;
import cmssi.lyson.handler.mapping.MappingConfiguration;

@LysonMapping(implicit=true)
public class SubMappedWithImplicitAndIdentityThd {

	@LysonMapping(mapping=MappingConfiguration.IDENTITY_MAPPING)
	private String name;
	
	private String subsubkey1;
	
	private String subsubkey2;
			
	public SubMappedWithImplicitAndIdentityThd(){}
	
	public String getSubsubkey1() {
		return this.subsubkey1;
	}
	
	public String getSubsubkey2() {
		return this.subsubkey2;
	}
	
	public String getName() {
		return this.name;
	}
}
