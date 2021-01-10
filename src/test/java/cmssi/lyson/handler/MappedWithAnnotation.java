package cmssi.lyson.handler;

import java.util.Map;

import cmssi.lyson.annotation.LysonMapping;

public class MappedWithAnnotation {

	@LysonMapping
	private SubMappedWithAnnotation key0;
	
	private String key1;
	
	@LysonMapping
	private String key2;

	private Map key3;
	
	@LysonMapping
	private char key4;

	@LysonMapping
	private char key5;
	
	@LysonMapping
	private char key6;

	@LysonMapping
	private boolean key7;

	@LysonMapping
	private boolean key8;

	@LysonMapping
	private boolean key9;

	@LysonMapping
	private byte key10;

	@LysonMapping
	private short key11;

	@LysonMapping
	private int key12;

	@LysonMapping
	private long key13;

	@LysonMapping
	private float key14;

	@LysonMapping
	private double key15;

	@LysonMapping
	private Class<Mapped> key16;
	
	@LysonMapping
	private Class<?> key17;

	@LysonMapping
	private Character key18;
	
	@LysonMapping
	private Boolean key19;
	
	@LysonMapping
	private byte key20;

	@LysonMapping
	private short key21;

	@LysonMapping
	private int key22;

	@LysonMapping
	private long key23;

	@LysonMapping
	private float key24;

	@LysonMapping
	private double key25;

	@LysonMapping
	private String key26;
	
	@LysonMapping
	private String key27;

	public MappedWithAnnotation(){}

	@LysonMapping
	public void setKey1(String key1) {
		this.key1=key1;
	}

	@LysonMapping
	public void setKey3(Map key3) {
		this.key3=key3;
	}
	
	public SubMappedWithAnnotation getKey0(){
		return this.key0;
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

	public char  getKey4() {
		return this.key4;
	}

	public char getKey5() {
		return this.key5;
	}

	public char  getKey6() {
		return this.key6;
	}

	public boolean getKey7() {
		return this.key7;
	}

	public boolean getKey8() {
		return this.key8;
	}

	public boolean getKey9() {
		return this.key9;
	}

	public byte getKey10() {
		return this.key10;
	}

	public short getKey11() {
		return this.key11;
	}

	public int getKey12() {
		return this.key12;
	}

	public long getKey13() {
		return this.key13;
	}

	public float getKey14() {
		return this.key14;
	}
	
	public double getKey15() {
		return this.key15;
	}	
	
	public Class<Mapped> getKey16() {
		return this.key16;
	}

	public Class<?> getKey17() {
		return this.key17;
	}
	
	public Character getKey18() {
		return this.key18;
	}
	
	public Boolean getKey19() {
		return this.key19;
	}

	public byte getKey20() {
		return this.key20;
	}

	public short getKey21() {
		return this.key21;
	}

	public int getKey22() {
		return this.key22;
	}

	public long getKey23() {
		return this.key23;
	}

	public float getKey24() {
		return this.key24;
	}

	public double getKey25() {
		return this.key25;
	}	

	public String getKey26() {
		return this.key26;
	}	
	
	public String getKey27() {
		return this.key27;
	}	

}
