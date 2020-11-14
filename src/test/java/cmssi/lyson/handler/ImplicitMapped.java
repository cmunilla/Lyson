package cmssi.lyson.handler;

import java.util.Map;

import cmssi.lyson.annotation.LysonMapping;

@LysonMapping(implicit=true)
public class ImplicitMapped {

	private String key1;
	
	@LysonMapping
	private String key2;
			
	@LysonMapping
	private Map key3;

	public ImplicitMapped(){}
	
	public String  getKey1() {
		return this.key1;
	}
	
	public String  getKey2() {
		return this.key2;
	}
	
	public Map  getKey3() {
		return this.key3;
	}
}
