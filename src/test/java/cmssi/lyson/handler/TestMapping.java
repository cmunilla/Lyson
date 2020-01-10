package cmssi.lyson.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import cmssi.lyson.LysonParser;
import cmssi.lyson.handler.MappingHandler;

public class TestMapping {

	@Test
	public void testMyAnnotatedMappedMappingJSON() {
		Logger.getLogger(MappingHandler.class.getName()).setLevel(Level.FINEST);
		MappingHandler<MyAnnotatedMapped> mapping = new MappingHandler<>(MyAnnotatedMapped.class);
		new LysonParser("{"
		+ "\"key1\":\"IT IS KEY ONE\\b\","
		+ "\"key2\": \"3\","
		+ "\"key3\":{\"fst\": 0.55, \"array\":[8,2,1,{\"thd\" => \"more\"}],"
		+ "\"key4\": \"\u00C8\",\"snd\":\"another\"},"
		+ "\"key5\": 128,"
		+ "\"key6\": 485668897.8779,"
		+ "\"key7\": \"true\","
		+ "\"key8\": 0.0001,"
		+ "\"key9\":  215,"
		+ "\"key10\":  "+String.valueOf(Byte.MAX_VALUE-1)+","
		+ "\"key11\":  "+String.valueOf(Short.MAX_VALUE-1)+","
		+ "\"key12\":  "+String.valueOf(Integer.MAX_VALUE-1)+","
		+ "\"key13\":  "+String.valueOf(Long.MAX_VALUE-1)+","
		+ "\"key14\":  "+String.valueOf(Float.MAX_VALUE-1)+","
		+ "\"key15\":  "+String.valueOf(Double.MAX_VALUE-1)+","
		+ "\"key16\": \"cmssi.lyson.handler.MyMapped\","
		+ "\"key17\": \"cmssi.lyson.handler.MyMapped3\","
		+ "\"key18\": 128,"
		+ "\"key19\": \"true\","
		+ "\"key20\": \"null\","
		+ "\"key21\": \"null\","
		+ "\"key22\": \"null\","
		+ "\"key23\": \"null\","
		+ "\"key24\": \"null\","
		+ "\"key25\": \"null\","
		+ "\"key26\": 44,"
		+ "\"key27\": null}"
		).parse(mapping);		
		MyAnnotatedMapped m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertTrue(List.class.isAssignableFrom(m.getKey3().get("array").getClass()));
		assertEquals("IT IS KEY ONE\b",m.getKey1());
		assertEquals("3",m.getKey2());
		assertEquals('\u00C8',m.getKey4());
		assertEquals(((char)128),m.getKey5());
		assertEquals(((char)0),m.getKey6());
		assertEquals(true,m.getKey7());
		assertEquals(false,m.getKey8());
		assertEquals(true,m.getKey9());
		assertEquals(((byte)Byte.MAX_VALUE-1) ,m.getKey10());
		assertEquals(((short)Short.MAX_VALUE-1) ,m.getKey11());
		assertEquals(((int)Integer.MAX_VALUE-1) ,m.getKey12());
		assertEquals(((long)Long.MAX_VALUE-1) ,m.getKey13());
		assertEquals(((float)Float.MAX_VALUE-1) ,m.getKey14(),0);
		assertEquals(((double)Double.MAX_VALUE-1) ,m.getKey15(),0);
		assertEquals(MyMapped.class,m.getKey16());
		assertEquals(null,m.getKey17());
		assertEquals(Character.valueOf(((char)128)),m.getKey18());
		assertEquals(Boolean.valueOf(true),m.getKey19());
		assertEquals(((byte)0) ,m.getKey20());
		assertEquals(((short)0) ,m.getKey21());
		assertEquals(((int)0) ,m.getKey22());
		assertEquals(((long)0) ,m.getKey23());
		assertEquals(((float)0) ,m.getKey24(),0);
		assertEquals(((double)0) ,m.getKey25(),0);
		assertEquals("44" ,m.getKey26());
		assertNull(m.getKey27());
	}

	@Test
	public void testMyMappedMappingJSON() {
		MappingHandler<MyMapped> mapping = new MappingHandler<>(MyMapped.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\b\",\"key3\":{\"fst\": 0.55,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
				).parse(mapping);		
		MyMapped m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertTrue(List.class.isAssignableFrom(m.getKey3().get("array").getClass()));
		assertEquals("IT IS KEY ONE\b",m.getKey1());
		assertEquals("3",m.getKey2());
	}

	@Test
	public void testMappingJSON() {
		MappingHandler mapping = new MappingHandler();
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\r\",\n\r\"key3\":{\"fst\": 0x5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
				).parse(mapping);		
		Map m = (Map) mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.get("key3").getClass()));
		assertTrue(List.class.isAssignableFrom(((Map)m.get("key3")).get("array").getClass()));
		assertEquals("IT IS KEY ONE\r",m.get("key1"));
		assertEquals("3",m.get("key2"));	
	}

	@Test
	public void testMyOtherMappedMappingJSON() {
		MappingHandler<MyOtherAnnotatedMapped> mapping = new MappingHandler<>(MyOtherAnnotatedMapped.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\f\",\"key3\":{\"fst\": 005,\"array\":[8,2,1,{\"thd\" => \"mor\\u00C8\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
				).parse(mapping);		
		MyOtherAnnotatedMapped m = mapping.getMapped();
		assertEquals(1,m.getKey3());
		assertEquals("IT IS KEY ONE\f",m.getKey2());
		assertEquals("3",m.getKey1());
	}
	
	@Test
	public void testSubMapping() {
		MappingHandler<MyMapped2> mapping = new MappingHandler<>(MyMapped2.class);
		new LysonParser("{\"key1\":\"val1\",\"key2\":\"val2\",\"key3\":{\"subkey1\":1,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}}}").parse(mapping);
		MyMapped2 m = mapping.getMapped();
		assertEquals("val1",m.getKey1());
		assertEquals("val2",m.getKey2());
		assertEquals(MySubMapped.class,m.getKey3().getClass());
		assertEquals(1,((MySubMapped)m.getKey3()).getSubKey1());
		assertEquals(MySubSubMapped.class,((MySubMapped)m.getKey3()).getSubKey2().getClass());
		assertEquals("subsubvalue1",((MySubSubMapped)((MySubMapped)m.getKey3()).getSubKey2()).getSubsubkey1());
		assertEquals("subsubvalue2",((MySubSubMapped)((MySubMapped)m.getKey3()).getSubKey2()).getSubsubkey2());
	}	
	
	@Test
	public void testObjectIntoObjectMappedMappingJSON() {
		MappingHandler mapping = new MappingHandler();
		new LysonParser("{\"key1\":{\"subKey1\":\"IT IS KEY ONE\\n\",\"subKey2\":{\"subsubKey\":"+String.valueOf((Long.MAX_VALUE-1))+"}},\"key2\":\"3\"}"
				).parse(mapping);		
		Map m = (Map) mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.get("key1").getClass()));
		assertTrue(Map.class.isAssignableFrom(((Map)m.get("key1")).get("subKey2").getClass()));
		assertEquals((Long.MAX_VALUE-1),((Map)((Map)m.get("key1")).get("subKey2")).get("subsubKey"));
		assertEquals("3",m.get("key2"));
	}

	@Test
	public void testMyOtherMappedInstantiatedMappingJSON() {
		MyOtherAnnotatedMapped mapped = new MyOtherAnnotatedMapped();
		MappingHandler<MyOtherAnnotatedMapped> mapping = new MappingHandler<>(mapped);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\t\",\"key3\":{\"fst\": 0.555555555555,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\\\"\"}"
				).parse(mapping);		
		MyOtherAnnotatedMapped m = mapping.getMapped();
		assertEquals(mapped, m);
		assertEquals(1,m.getKey3());
		assertEquals("IT IS KEY ONE\t",m.getKey2());
		assertEquals("3\"",m.getKey1());
	}
	
	@Test
	public void testMappingBooleans() {
		MappingHandler mapping = new MappingHandler();
		new LysonParser("{\"key1\":TruE,\"key2\":faLSE,\"key3\":\"TRUE\"}").parse(mapping);		
		Map m = (Map) mapping.getMapped();
		assertEquals(true, m.get("key1"));
		assertEquals(false,m.get("key2"));
		assertEquals("TRUE",m.get("key3"));
	}
	
	@Test
	public void testMappingNulls() {
		MappingHandler mapping = new MappingHandler();
		new LysonParser("{\"key1\":null,\"key2\":\"\",\"key3\":\"null\"}").parse(mapping);		
		Map m = (Map) mapping.getMapped();
		assertNull(m.get("key1"));
		assertEquals("",m.get("key2"));
		assertEquals("null",m.get("key3"));
	}

	@Test
	public void testMultiMappedEmptyMappingRootArray() throws FileNotFoundException {
		MappingHandler<MyEmptyAnnotatedTypeMapped> mapping = new MappingHandler<>(MyEmptyAnnotatedTypeMapped.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootarray.json"))).parse(mapping);		
		MyEmptyAnnotatedTypeMapped m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertEquals(12,((Map)m.getKey3()).get("subkey1"));
		assertEquals("val12",m.getKey1());
		assertEquals("val202",m.getKey2());	
	}

	@Test
	public void testMultiMappedEmptyMappingRootObject() throws FileNotFoundException {
		MappingHandler<MyEmptyAnnotatedTypeMapped> mapping = new MappingHandler<>(MyEmptyAnnotatedTypeMapped.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootobject.json"))).parse(mapping);		
		MyEmptyAnnotatedTypeMapped m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertEquals(12,((Map)m.getKey3()).get("subkey1"));
		assertEquals("val12",m.getKey1());
		assertEquals("val202",m.getKey2());	
	}

	@Test
	public void testMultiMappedMappingRootArray() throws FileNotFoundException {
		MappingHandler<MyAnnotatedTypeMapped> mapping = new MappingHandler<MyAnnotatedTypeMapped>(MyAnnotatedTypeMapped.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootarray.json"))).parse(mapping);		
		List<MyAnnotatedTypeMapped> m = mapping.getMapped();
		assertEquals(12, m.size());
		MyAnnotatedTypeMapped p = m.get(11);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(1,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val1",p.getKey1());
		assertEquals("val2",p.getKey2());

		p = m.get(10);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(2,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val2",p.getKey1());
		assertEquals("val21",p.getKey2());

		p = m.get(9);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(3,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val3",p.getKey1());
		assertEquals("val22",p.getKey2());

		p = m.get(8);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(4,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val4",p.getKey1());
		assertEquals("val23",p.getKey2());

		p = m.get(7);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(5,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val5",p.getKey1());
		assertEquals("val24",p.getKey2());

		p = m.get(6);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(6,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val6",p.getKey1());
		assertEquals("val25",p.getKey2());

		p = m.get(5);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(7,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val7",p.getKey1());
		assertEquals("val26",p.getKey2());

		p = m.get(4);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(8,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val8",p.getKey1());
		assertEquals("val27",p.getKey2());

		p = m.get(3);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(9,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val9",p.getKey1());
		assertEquals("val28",p.getKey2());

		p = m.get(2);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(10,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val10",p.getKey1());
		assertEquals("val29",p.getKey2());

		p = m.get(1);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(11,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val11",p.getKey1());
		assertEquals("val201",p.getKey2());

		p = m.get(0);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(12,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val12",p.getKey1());
		assertEquals("val202",p.getKey2());
	}

	@Test
	public void testMultiMappedMappingRootObject() throws FileNotFoundException {
		MappingHandler<MyAnnotatedTypeMapped> mapping = new MappingHandler<>(MyAnnotatedTypeMapped.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootobject.json"))).parse(mapping);		
		List<MyAnnotatedTypeMapped> m = mapping.getMapped();
		assertEquals(12, m.size());
		MyAnnotatedTypeMapped p = m.get(11);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(1,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val1",p.getKey1());
		assertEquals("val2",p.getKey2());

		p = m.get(10);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(2,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val2",p.getKey1());
		assertEquals("val21",p.getKey2());

		p = m.get(9);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(3,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val3",p.getKey1());
		assertEquals("val22",p.getKey2());

		p = m.get(8);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(4,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val4",p.getKey1());
		assertEquals("val23",p.getKey2());

		p = m.get(7);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(5,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val5",p.getKey1());
		assertEquals("val24",p.getKey2());

		p = m.get(6);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(6,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val6",p.getKey1());
		assertEquals("val25",p.getKey2());

		p = m.get(5);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(7,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val7",p.getKey1());
		assertEquals("val26",p.getKey2());

		p = m.get(4);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(8,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val8",p.getKey1());
		assertEquals("val27",p.getKey2());

		p = m.get(3);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(9,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val9",p.getKey1());
		assertEquals("val28",p.getKey2());

		p = m.get(2);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(10,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val10",p.getKey1());
		assertEquals("val29",p.getKey2());

		p = m.get(1);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(11,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val11",p.getKey1());
		assertEquals("val201",p.getKey2());

		p = m.get(0);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(12,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val12",p.getKey1());
		assertEquals("val202",p.getKey2());
	}

	@Test
	public void testMultiMappedMappingArray() throws FileNotFoundException {
		MappingHandler<MyOtherAnnotatedTypeMapped> mapping = new MappingHandler<>(MyOtherAnnotatedTypeMapped.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multiarray.json"))).parse(mapping);		
		List<MyOtherAnnotatedTypeMapped> m = mapping.getMapped();
		assertEquals(12, m.size());
		MyOtherAnnotatedTypeMapped p = m.get(11);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(1,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val1",p.getKey1());
		assertEquals("val2",p.getKey2());

		p = m.get(10);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(2,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val2",p.getKey1());
		assertEquals("val21",p.getKey2());

		p = m.get(9);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(3,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val3",p.getKey1());
		assertEquals("val22",p.getKey2());

		p = m.get(8);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(4,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val4",p.getKey1());
		assertEquals("val23",p.getKey2());

		p = m.get(7);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(5,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val5",p.getKey1());
		assertEquals("val24",p.getKey2());

		p = m.get(6);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(6,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val6",p.getKey1());
		assertEquals("val25",p.getKey2());

		p = m.get(5);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(7,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val7",p.getKey1());
		assertEquals("val26",p.getKey2());

		p = m.get(4);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(8,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val8",p.getKey1());
		assertEquals("val27",p.getKey2());

		p = m.get(3);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(9,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val9",p.getKey1());
		assertEquals("val28",p.getKey2());

		p = m.get(2);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(10,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val10",p.getKey1());
		assertEquals("val29",p.getKey2());

		p = m.get(1);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(11,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val11",p.getKey1());
		assertEquals("val201",p.getKey2());

		p = m.get(0);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(12,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val12",p.getKey1());
		assertEquals("val202",p.getKey2());
	}
	
	@Test
	public void testMultiMappedMappingObject() throws FileNotFoundException {
		MappingHandler<MyOtherAnnotatedTypeMapped> mapping = new MappingHandler<>(MyOtherAnnotatedTypeMapped.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multiobject.json"))).parse(mapping);		
		List<MyOtherAnnotatedTypeMapped> m = mapping.getMapped();
		assertEquals(12, m.size());
		MyOtherAnnotatedTypeMapped p = m.get(11);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(1,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val1",p.getKey1());
		assertEquals("val2",p.getKey2());

		p = m.get(10);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(2,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val2",p.getKey1());
		assertEquals("val21",p.getKey2());

		p = m.get(9);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(3,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val3",p.getKey1());
		assertEquals("val22",p.getKey2());

		p = m.get(8);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(4,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val4",p.getKey1());
		assertEquals("val23",p.getKey2());

		p = m.get(7);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(5,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val5",p.getKey1());
		assertEquals("val24",p.getKey2());

		p = m.get(6);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(6,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val6",p.getKey1());
		assertEquals("val25",p.getKey2());

		p = m.get(5);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(7,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val7",p.getKey1());
		assertEquals("val26",p.getKey2());

		p = m.get(4);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(8,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val8",p.getKey1());
		assertEquals("val27",p.getKey2());

		p = m.get(3);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(9,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val9",p.getKey1());
		assertEquals("val28",p.getKey2());

		p = m.get(2);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(10,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val10",p.getKey1());
		assertEquals("val29",p.getKey2());

		p = m.get(1);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(11,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val11",p.getKey1());
		assertEquals("val201",p.getKey2());

		p = m.get(0);
		assertTrue(Map.class.isAssignableFrom(p.getKey3().getClass()));
		assertEquals(12,((Map)p.getKey3()).get("subkey1"));
		assertEquals("val12",p.getKey1());
		assertEquals("val202",p.getKey2());
	}
}
