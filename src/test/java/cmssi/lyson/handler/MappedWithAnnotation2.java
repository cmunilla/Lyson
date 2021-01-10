package cmssi.lyson.handler;

import cmssi.lyson.annotation.LysonMapping;

public class MappedWithAnnotation2 {

	@LysonMapping(mapping="key2")
	private String key1;
	
	@LysonMapping(mapping="key1")
	private String key2;
			
	@LysonMapping(mapping="/key3/array/[0]")
	private int key3;

	public MappedWithAnnotation2(){}
	
	public String  getKey1() {
		return this.key1;
	}
	
	public String  getKey2() {
		return this.key2;
	}
	
	public int getKey3() {
		return this.key3;
	}
}
