package cmssi.lyson.handler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cmssi.lyson.LysonParser;

public class TestValidation {

	@Test
	public void testEmptyArrayValidJSON() {
		assertTrue(new LysonParser("[]") .valid());
	}
	
	@Test
	public void testEmptyObjectValidJSON() {
		assertTrue(new LysonParser("{}").valid());
	}
	
	@Test
	public void testValidJSON() {
		assertTrue(new LysonParser("[8,{\"fst\": 5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\",\"frth\":{\"last\":45}}]") .valid());
	}

	@Test
	public void testInvalidJSON() {
		assertFalse(new LysonParser("[8,{\"fst\": 5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\",{\"last\":45}}]") .valid());
	}
}
