package cmssi.lyson.handler;

import java.util.Map;

import cmssi.lyson.annotation.LysonMapping;

@LysonMapping(mapping="/[2]/*")
public class MappedWithPrefix2 {

	private String key1;
	
	@LysonMapping
	private String key2;
			
	@LysonMapping
	private Map key3;

	public MappedWithPrefix2(){}
	
	@LysonMapping
	public void setKey1(String key1) {
		this.key1=key1;
	}

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
