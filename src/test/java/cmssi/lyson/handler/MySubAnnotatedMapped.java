package cmssi.lyson.handler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import cmssi.lyson.annotation.LysonMapping;

public class MySubAnnotatedMapped {

	@LysonMapping(mapping="/key0/[0]")
	private String key1;

	@LysonMapping(mapping="/key0/[1]")
	private Float key2;

	@LysonMapping(mapping="/key0/[2]")
	private String key3;

	@LysonMapping(mapping="/key0/[3]")
	private List key4;

	@LysonMapping(mapping="/key0/[4]")
	private char key5;

	@LysonMapping(mapping="/key0/[5]")
	private Double key6;
	
	@LysonMapping(mapping="/key0/[6]")
	private BigInteger key7;
	
	@LysonMapping(mapping="/key0/[7]")
	private BigDecimal key8;

	@LysonMapping(mapping="/key0/[8]")
	private Byte key9;
	
	@LysonMapping(mapping="/key0/[9]")
	private Short key10;
	
	@LysonMapping(mapping="/key0/[10]")
	private Integer key11;

	@LysonMapping(mapping="/key0/[11]")
	private Long key12;
	

	public MySubAnnotatedMapped(){}

	public String  getKey1() {
		return this.key1;
	}
	
	public Float  getKey2() {
		return this.key2;
	}

	public String  getKey3() {
		return this.key3;
	}

	public List  getKey4() {
		return this.key4;
	}

	public char  getKey5() {
		return this.key5;
	}
	
	public Double  getKey6() {
		return this.key6;
	}

	public BigInteger  getKey7() {
		return this.key7;
	}

	public BigDecimal  getKey8() {
		return this.key8;
	}
	
	public Byte  getKey9() {
		return this.key9;
	}
	
	public Short getKey10() {
		return this.key10;
	}
	
	public Integer getKey11() {
		return this.key11;
	}
	
	public Long getKey12() {
		return this.key12;
	}
}
