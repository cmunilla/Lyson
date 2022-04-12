package cmssi.lyson.handler;

import java.util.Map;

import cmssi.lyson.annotation.LysonMapping;
import cmssi.lyson.annotation.LysonIgnore;

@LysonMapping(implicit=true)
public class MappedWithImplicitAndIgnore {

	private String key1;
	
	@LysonMapping
	private String key2;
			
	@LysonMapping
	private Map key3;

	@LysonIgnore
	private String key4;

	public MappedWithImplicitAndIgnore(){}
	
	public String  getKey1() {
		return this.key1;
	}
	
	public String  getKey2() {
		return this.key2;
	}

	public Map  getKey3() {
		return this.key3;
	}
	
	public String getKey4() {
		return this.key4;
	}
}
