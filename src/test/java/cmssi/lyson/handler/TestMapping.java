package cmssi.lyson.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import cmssi.lyson.LysonParser;
import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.handler.mapping.MappingHandler;

public class TestMapping {

	@Test
	public void testMyAnnotatedMappedMappingJSON() {
		Logger.getLogger(MappingHandler.class.getName()).setLevel(Level.FINEST);
		MappingHandler mapping = new MappingHandler(MappedWithAnnotation.class);
		new LysonParser("{"
		+ "\"key0\":[\"fst\", 0.55, \"array\",[8,2,1,{\"thd\" => \"more\"}], true, 1.225544, 1000789446512301, 44.2222222222222222228,5,1024,65535,45654654654654],"
		+ "\"key1\":\"IT IS KEY ONE\\b\","
		+ "\"key2\": \"3\","
		+ "\"key3\":{\"fst\": 0.55, \"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},"
		+ "\"key4\": \"\u00C8\","
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
		+ "\"key16\": \"cmssi.lyson.handler.Mapped\","
		+ "\"key17\": \"cmssi.lyson.handler.Mapped2\","
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
		MappedWithAnnotation m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertEquals("fst", m.getKey0().getKey1());
		assertEquals(0.55f, m.getKey0().getKey2(),0);
		assertEquals("array", m.getKey0().getKey3());
		assertTrue(List.class.isAssignableFrom(m.getKey0().getKey4().getClass()));
		assertEquals(4,((List)m.getKey0().getKey4()).size());
		assertEquals('1', m.getKey0().getKey5());
		assertEquals(Double.valueOf(1.225544d), m.getKey0().getKey6(),0);
		assertEquals(new BigInteger("1000789446512301"), m.getKey0().getKey7());
		assertEquals(new BigDecimal("44.22222222222222"), m.getKey0().getKey8());
		assertEquals(Byte.valueOf("5"), m.getKey0().getKey9());
		assertEquals(Short.valueOf("1024"), m.getKey0().getKey10());
		assertEquals(Integer.valueOf("65535"), m.getKey0().getKey11());
		assertEquals(Long.valueOf("45654654654654"), m.getKey0().getKey12());
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
		assertEquals(Mapped.class,m.getKey16());
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
	public void testMultiMapped() {
		MappingHandler myMappedMapping = new MappingHandler(Mapped.class);
		MappingHandler rawMapping = new MappingHandler();
		MappingHandler myOtherAnnotatedMappedMapping = new MappingHandler(MappedWithAnnotation2.class);
		MappingHandler myMapped2Mapping = new MappingHandler(MappedWithSubMapped.class);
		
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\b\",\"key3\":{\"fst\": 0.55,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
			).parse(
			myMappedMapping,
			rawMapping,
			new MappingHandler() {
				private int count = 0;
				@Override
				public boolean handle(ParsingEvent event) {
					if(count++ == 10) {
						return false;
					}
					return true;
				}
			},
			myOtherAnnotatedMappedMapping,
			myMapped2Mapping,
			new MappingHandler() {
				private int count = 0;
				@Override
				public boolean handle(ParsingEvent event) {
					if(count++ == 3) {
						return false;
					}
					return true;
				}
			});	
	
		MappedWithSubMapped m = myMapped2Mapping.getMapped();
		assertEquals("IT IS KEY ONE\b",m.getKey1());
		assertEquals("3",m.getKey2());
		assertEquals(SubMapped.class,m.getKey3().getClass());
		assertEquals(0,((SubMapped)m.getKey3()).getSubKey1());
		assertNull(((SubMapped)m.getKey3()).getSubKey2());
		
		Map l = (Map) rawMapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(l.get("key3").getClass()));
		assertTrue(List.class.isAssignableFrom(((Map)l.get("key3")).get("array").getClass()));
		assertEquals("IT IS KEY ONE\b",l.get("key1"));
		assertEquals("3",l.get("key2"));	
		
		MappedWithAnnotation2 v = myOtherAnnotatedMappedMapping.getMapped();
		assertEquals(8,v.getKey3());
		assertEquals("IT IS KEY ONE\b",v.getKey2());
		assertEquals("3",v.getKey1());
		
		Mapped w = myMappedMapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(w.getKey3().getClass()));
		assertTrue(List.class.isAssignableFrom(w.getKey3().get("array").getClass()));
		assertEquals("IT IS KEY ONE\b",w.getKey1());
		assertEquals("3",w.getKey2());
	}
	
	@Test
	public void testMyMappedMappingJSON() {
		MappingHandler mapping = new MappingHandler(Mapped.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\b\",\"key3\":{\"fst\": 0.55,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
				).parse(mapping);		
		Mapped m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertTrue(List.class.isAssignableFrom(m.getKey3().get("array").getClass()));
		assertEquals("IT IS KEY ONE\b",m.getKey1());
		assertEquals("3",m.getKey2());
	}
	
	@Test
	public void testMyMappedWithIntegerQueue() {
		MappingHandler mapping = new MappingHandler(MappedWithQueueOfInteger.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\b\",\"key3\":[2,8,9,7,6],\"key2\":\"3\"}"
				).parse(mapping);		
		MappedWithQueueOfInteger m = mapping.getMapped();
		assertTrue(Queue.class.isAssignableFrom(m.getKey3().getClass()));
		int[] queue = new int[] {2,8,9,7,6};
		int pos=0;
		for(Iterator it =((Queue)m.getKey3()).iterator();it.hasNext();)
			assertEquals(queue[pos++],it.next());
		assertEquals("IT IS KEY ONE\b",m.getKey1());
		assertEquals("3",m.getKey2());
	}

	@Test
	public void testMyMappedWithIntegerVector() {
		MappingHandler mapping = new MappingHandler(MappedWithVectorOfInteger.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\b\",\"key3\":[2,8,9,7,6],\"key2\":\"3\"}"
				).parse(mapping);		
		MappedWithVectorOfInteger m = mapping.getMapped();
		assertTrue(Vector.class.isAssignableFrom(m.getKey3().getClass()));
		int[] queue = new int[] {2,8,9,7,6};
		int pos=0;
		for(Iterator it =((Vector)m.getKey3()).iterator();it.hasNext();)
			assertEquals(queue[pos++],it.next());
		assertEquals("IT IS KEY ONE\b",m.getKey1());
		assertEquals("3",m.getKey2());
	}
	
	@Test
	public void testMyMappedWithIntegerArray() {
		MappingHandler mapping = new MappingHandler(MappedWithArrayOfInteger.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\b\",\"key3\":[2,8,9,7,6],\"key2\":\"3\"}"
				).parse(mapping);		
		MappedWithArrayOfInteger m = mapping.getMapped();
		assertTrue(m.getKey3().getClass().isArray());
		assertEquals(Integer.class, m.getKey3().getClass().getComponentType());
		int[] queue = new int[] {2,8,9,7,6};
		int pos=0;
		for(int i = 0;i < m.getKey3().length;i++)
			assertEquals(Integer.valueOf(queue[pos++]),m.getKey3()[i]);
		assertEquals("IT IS KEY ONE\b",m.getKey1());
		assertEquals("3",m.getKey2());
	}

	@Test
	public void testMyMappedWithPrimitveIntegerArray() {
		MappingHandler mapping = new MappingHandler(MappedWithArrayOfPrimitiveInteger.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\b\",\"key3\":[2,8,9,7,6],\"key2\":\"3\"}"
				).parse(mapping);		
		MappedWithArrayOfPrimitiveInteger m = mapping.getMapped();
		assertTrue(m.getKey3().getClass().isArray());
		assertEquals(int.class, m.getKey3().getClass().getComponentType());
		int[] queue = new int[] {2,8,9,7,6};
		int pos=0;
		for(int i = 0;i < m.getKey3().length;i++)
			assertEquals(queue[pos++],m.getKey3()[i]);
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
		MappingHandler mapping = new MappingHandler(MappedWithAnnotation2.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\f\",\"key3\":{\"fst\": 005,\"array\":[8,2,1,{\"thd\" => \"mor\\u00C8\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
				).parse(mapping);		
		MappedWithAnnotation2 m = mapping.getMapped();
		assertEquals(8,m.getKey3());
		assertEquals("IT IS KEY ONE\f",m.getKey2());
		assertEquals("3",m.getKey1());
	}
	
	@Test
	public void testSubMapping() {
		MappingHandler mapping = new MappingHandler(MappedWithSubMapped.class);
		new LysonParser("{\"key1\":\"val1\",\"key2\":\"val2\",\"key3\":{\"subkey1\":1,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}}}").parse(mapping);
		MappedWithSubMapped m = mapping.getMapped();
		assertEquals("val1",m.getKey1());
		assertEquals("val2",m.getKey2());
		assertEquals(SubMapped.class,m.getKey3().getClass());
		assertEquals(1,((SubMapped)m.getKey3()).getSubKey1());
		assertEquals(SubMappedSnd.class,((SubMapped)m.getKey3()).getSubKey2().getClass());
		assertEquals("subsubvalue1",((SubMappedSnd)((SubMapped)m.getKey3()).getSubKey2()).getSubsubkey1());
		assertEquals("subsubvalue2",((SubMappedSnd)((SubMapped)m.getKey3()).getSubKey2()).getSubsubkey2());
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
		MappedWithAnnotation2 mapped = new MappedWithAnnotation2();
		MappingHandler mapping = new MappingHandler(mapped);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\\t\",\"key3\":{\"fst\": 0.555555555555,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\\\"\"}"
				).parse(mapping);		
		MappedWithAnnotation2 m = mapping.getMapped();
		assertEquals(mapped, m);
		assertEquals(8,m.getKey3());
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
		MappingHandler mapping = new MappingHandler(MappedWithEmptyPrefix.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootarray.json"))).parse(mapping);		
		MappedWithEmptyPrefix m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertEquals(12,((Map)m.getKey3()).get("subkey1"));
		assertEquals("val12",m.getKey1());
		assertEquals("val202",m.getKey2());	
	}

	@Test
	public void testMultiMappedEmptyMappingRootObject() throws FileNotFoundException {
		MappingHandler mapping = new MappingHandler(MappedWithEmptyPrefix.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootobject.json"))).parse(mapping);		
		MappedWithEmptyPrefix m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertEquals(12,((Map)m.getKey3()).get("subkey1"));
		assertEquals("val12",m.getKey1());
		assertEquals("val202",m.getKey2());	
	}

	@Test
	public void testMultiMappedMappingRootArray() throws FileNotFoundException {
		MappingHandler mapping = new MappingHandler(MappedWithPrefix.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootarray.json"))).parse(mapping);		
		List<MappedWithPrefix> m = mapping.getMapped();
		assertEquals(12, m.size());
		MappedWithPrefix p = m.get(11);
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
	public void testIdentityMappedMapping() {				
		String json =  "{\"key1\":\"val1\",\"key2\":\"val2\",\"key3\":{\"subkey1\":1,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}}}";
		MappingHandler mapping = new MappingHandler(MappedWithSubMappedIdentity.class);
		new LysonParser(json).parse(mapping);	
		MappedWithSubMappedIdentity im = mapping.getMapped();
		assertEquals("val1",im.getKey1());
		assertEquals("val2",im.getKey2());
		assertTrue(SubMappedWithImplicitAndIdentitySnd.class.isAssignableFrom(im.getKey3().getClass()));
		SubMappedWithImplicitAndIdentitySnd ism = im.getKey3();
		assertEquals("key3",ism.getName());
		assertEquals(1 , ism.getSubKey1());
		assertTrue(SubMappedWithImplicitAndIdentityThd.class.isAssignableFrom(ism.getSubKey2().getClass()));
		SubMappedWithImplicitAndIdentityThd issm = ism.getSubKey2();
		assertEquals("subkey2",issm.getName());
		assertEquals("subsubvalue1",issm.getSubsubkey1());
		assertEquals("subsubvalue2",issm.getSubsubkey2());
	}

	@Test
	public void testIdentityMapMapping() {				
		String json =  "{\"key1\":\"val1\",\"key2\":\"val2\",\"key3\":{\"subkey1\":1,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}}}";
		MappingHandler mapping = new MappingHandler(true);
		new LysonParser(json).parse(mapping);	
		Map im =  (Map)mapping.getMapped();
		assertEquals("val1",im.get("key1"));
		assertEquals("val2",im.get("key2"));
		assertTrue(Map.class.isAssignableFrom(im.get("key3").getClass()));
		Map ism = (Map) im.get("key3");
		assertEquals("key3",ism.get("identity"));
		assertEquals(1 , ism.get("subkey1"));
		assertTrue(Map.class.isAssignableFrom(ism.get("subkey2").getClass()));
		Map issm = (Map) ism.get("subkey2");
		assertEquals("subkey2",issm.get("identity"));
		assertEquals("subsubvalue1",issm.get("subsubkey1"));
		assertEquals("subsubvalue2",issm.get("subsubkey2"));
	}

	@Test
	public void testIdentityListMapping() {				
		String json =  "{\"key1\":\"val1\",\"key2\":\"val2\",\"key3\":{\"subkey1\":1,\"subkey2\":[\"subsubvalue1\",\"subsubvalue2\"]}}";
		MappingHandler mapping = new MappingHandler(true);
		new LysonParser(json).parse(mapping);	
		Map im =  (Map)mapping.getMapped();
		assertEquals("val1",im.get("key1"));
		assertEquals("val2",im.get("key2"));
		assertTrue(Map.class.isAssignableFrom(im.get("key3").getClass()));
		Map ism = (Map) im.get("key3");
		assertEquals("key3",ism.get("identity"));
		assertEquals(1 , ism.get("subkey1"));
		assertTrue(List.class.isAssignableFrom(ism.get("subkey2").getClass()));
		List issm = (List) ism.get("subkey2");
		assertEquals("subsubvalue1",issm.get(0));
		assertEquals("subsubvalue2",issm.get(1));
	}
	
	@Test
	public void testImplicitMappedMapping() throws FileNotFoundException {
		String json =  "{\"key1\":\"val1\",\"key2\":\"val2\",\"key3\":{\"subkey1\":1,\"subkey2\":5}}";
		MappingHandler mapping = new MappingHandler(MappedWithImplicit.class);
		new LysonParser(json).parse(mapping);	
		MappedWithImplicit im = mapping.getMapped();
		assertEquals("val1",im.getKey1());
		assertEquals("val2",im.getKey2());
		assertTrue(Map.class.isAssignableFrom(im.getKey3().getClass()));
		assertEquals(5,((Map)im.getKey3()).get("subkey2"));

		MappingHandler mapping2 = new MappingHandler(MappedWithoutImplicit.class);
		new LysonParser(json).parse(mapping2);	
		MappedWithoutImplicit im2 = mapping2.getMapped();
		assertNull(im2.getKey1());
		assertEquals("val2",im2.getKey2());
		assertTrue(Map.class.isAssignableFrom(im2.getKey3().getClass()));
		assertEquals(5,((Map)im2.getKey3()).get("subkey2"));
	}
	
	@Test
	public void testMultiIdentityMappedObject() throws FileNotFoundException {
		MappingHandler mapping = new MappingHandler(true);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootobject.json"))).parse(mapping);		
		Map m = mapping.getMapped();
		assertEquals(12, m.size());
		Map p = (Map) m.get("11");
		assertTrue(Map.class.isAssignableFrom(p.get("key3").getClass()));
		assertEquals(12,((Map)p.get("key3")).get("subkey1"));
		assertEquals("11",p.get("identity"));
		assertEquals("val12",p.get("key1"));
		assertEquals("val202",p.get("key2"));
	}

	@Test
	public void testListSubMappedMapping() {
		MappingHandler mapping = new MappingHandler(MappedWithListOfSubMapped.class);
		new LysonParser("{\"key1\":\"val1\",\"key2\":\"val2\","
				+ "\"key3\":["
				+ "{\"subkey1\":1,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}},"
				+ "{\"subkey1\":2,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}},"
				+ "{\"subkey1\":3,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}}"
				+ "]}"
		).parse(mapping);
		MappedWithListOfSubMapped m = mapping.getMapped();
		assertEquals("val1",m.getKey1());
		assertEquals("val2",m.getKey2());
		assertTrue(List.class.isAssignableFrom(m.getKey3().getClass()));
		assertTrue(SubMapped.class.isAssignableFrom(m.getKey3().get(0).getClass()));
		assertEquals(1,m.getKey3().get(0).getSubKey1());
		assertEquals(2,m.getKey3().get(1).getSubKey1());
		assertEquals(3,m.getKey3().get(2).getSubKey1());
	}	

	@Test
	public void testArraySubMappedMapping() {
		MappingHandler mapping = new MappingHandler(MappedWithArrayOfSubMapped.class);
		new LysonParser("{\"key1\":\"val1\",\"key2\":\"val2\","
				+ "\"key3\":["
				+ "{\"subkey1\":1,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}},"
				+ "{\"subkey1\":2,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}},"
				+ "{\"subkey1\":3,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}}"
				+ "]}"
		).parse(mapping);
		MappedWithArrayOfSubMapped m = mapping.getMapped();
		assertEquals("val1",m.getKey1());
		assertEquals("val2",m.getKey2());
		assertTrue(m.getKey3().getClass().isArray());
		assertTrue(SubMapped[].class.isAssignableFrom(m.getKey3().getClass()));
		assertEquals(1,m.getKey3()[0].getSubKey1());
		assertEquals(2,m.getKey3()[1].getSubKey1());
		assertEquals(3,m.getKey3()[2].getSubKey1());
	}	

	@Test
	public void testMapSubMappedMapping() {
		MappingHandler mapping = new MappingHandler(MappedWithMapOfSubMapped.class);
		new LysonParser("{\"key1\":\"val1\",\"key2\":\"val2\","
				+ "\"key3\":{"
				+ "\"subone\":{\"subkey1\":1,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}},"
				+ "\"subtwo\":{\"subkey1\":2,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}},"
				+ "\"subthree\":{\"subkey1\":3,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}}"
				+ "}"
				+ "}"
		).parse(mapping);
		MappedWithMapOfSubMapped m = mapping.getMapped();
		assertEquals("val1",m.getKey1());
		assertEquals("val2",m.getKey2());
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertTrue(SubMappedWithImplicitAndIdentityFst.class.isAssignableFrom(m.getKey3().get("subone").getClass()));
		assertEquals(1,m.getKey3().get("subone").getSubKey1());
		assertEquals(2,m.getKey3().get("subtwo").getSubKey1());
		assertEquals(3,m.getKey3().get("subthree").getSubKey1());
	}	

	@Test
	public void testDictionarySubMappedMapping() {
		MappingHandler mapping = new MappingHandler(MappedWithDictionaryOfSubMapped.class);
		new LysonParser("{\"key1\":\"val1\",\"key2\":\"val2\","
				+ "\"key3\":{"
				+ "\"subone\":{\"subkey1\":1,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}},"
				+ "\"subtwo\":{\"subkey1\":2,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}},"
				+ "\"subthree\":{\"subkey1\":3,\"subkey2\":{\"subsubkey1\":\"subsubvalue1\",\"subsubkey2\":\"subsubvalue2\"}}"
				+ "}"
				+ "}"
		).parse(mapping);
		MappedWithDictionaryOfSubMapped m = mapping.getMapped();
		assertEquals("val1",m.getKey1());
		assertEquals("val2",m.getKey2());
		assertTrue(Dictionary.class.isAssignableFrom(m.getKey3().getClass()));
		assertTrue(SubMappedWithImplicitAndIdentityFst.class.isAssignableFrom(m.getKey3().get("subone").getClass()));
		assertEquals(1,m.getKey3().get("subone").getSubKey1());
		assertEquals(2,m.getKey3().get("subtwo").getSubKey1());
		assertEquals(3,m.getKey3().get("subthree").getSubKey1());
	}	
	
	@Test
	public void testMultiMappedMappingRootObject() throws FileNotFoundException {
		MappingHandler mapping = new MappingHandler(MappedWithPrefix.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multirootobject.json"))).parse(mapping);		
		List<MappedWithPrefix> m = mapping.getMapped();
		assertEquals(12, m.size());
		MappedWithPrefix p = m.get(11);
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
		MappingHandler mapping = new MappingHandler(MappedWithPrefix2.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multiarray.json"))).parse(mapping);		
		List<MappedWithPrefix2> m = mapping.getMapped();
		assertEquals(12, m.size());
		MappedWithPrefix2 p = m.get(11);
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
		MappingHandler mapping = new MappingHandler(MappedWithPrefix2.class);
		new LysonParser(new FileInputStream(new File("src/test/resources/multiobject.json"))).parse(mapping);		
		List<MappedWithPrefix2> m = mapping.getMapped();
		assertEquals(12, m.size());
		MappedWithPrefix2 p = m.get(11);
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
		assertEquals("val3", p.getKey1());
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
