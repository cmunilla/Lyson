package cmssi.lyson.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import cmssi.lyson.LysonParser;
import cmssi.lyson.handler.MappingHandler;

public class TestMapping {

	@Test
	public void testMyMappedMappingJSON() {
		MappingHandler mapping = new MappingHandler(MyMapped.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\",\"key3\":{\"fst\": 5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
				).parse(mapping);		
		MyMapped m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.getKey3().getClass()));
		assertTrue(List.class.isAssignableFrom(m.getKey3().get("array").getClass()));
		assertEquals("IT IS KEY ONE",m.getKey1());
		assertEquals("3",m.getKey2());
	}

	@Test
	public void testMappingJSON() {
		MappingHandler mapping = new MappingHandler();
		new LysonParser("{\"key1\":\"IT IS KEY ONE\",\"key3\":{\"fst\": 5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
				).parse(mapping);		
		Map m = mapping.getMapped();
		assertTrue(Map.class.isAssignableFrom(m.get("key3").getClass()));
		assertTrue(List.class.isAssignableFrom(((Map)m.get("key3")).get("array").getClass()));
		assertEquals("IT IS KEY ONE",m.get("key1"));
		assertEquals("3",m.get("key2"));	
	}

	@Test
	public void testMyOtherMappedMappingJSON() {
		MappingHandler mapping = new MappingHandler(MyOtherMapped.class);
		new LysonParser("{\"key1\":\"IT IS KEY ONE\",\"key3\":{\"fst\": 5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\"},\"key2\":\"3\"}"
				).parse(mapping);		
		MyOtherMapped m = mapping.getMapped();
		assertEquals(1,m.getKey3());
		assertEquals("IT IS KEY ONE",m.getKey2());
		assertEquals("3",m.getKey1());
	}

}
