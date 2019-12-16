package cmssi.lyson.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import cmssi.lyson.LysonParser;
import cmssi.lyson.exception.LysonParsingException;

public class TestValidation {

	@Test
	public void testLysonParserConstructorWithInputStream() {
		assertTrue(new LysonParser(new ByteArrayInputStream("[6,7,9,2]".getBytes())).valid());
	}	
	
	@Test
	public void testEmptyArrayValidJSON() {
		assertTrue(new LysonParser("[]") .valid());
	}
	
	@Test
	public void testEmptyObjectValidJSON() {
		assertTrue(new LysonParser("{}").valid());
	}

	@Test
	public void testValidJSONObjectArrowKeyValueSeparator() {
		assertTrue(new LysonParser("{\"fst\"=> 5,\"snd\":12,\"last\"=>\"45\"}").valid());
	}
	
	@Test
	public void testValidJSONArrayWithOneMissingValue() {
		assertTrue(new LysonParser("[5,,45,12]").valid());
	}

	@Test
	public void testValidJSONArrayWithTwoFollowingMissingValues() {
		assertTrue(new LysonParser("[5,,,45,12]") .valid());
	}
	
	@Test
	public void testInvalidJSONObjectMissingKey() {		
		assertFalse(new LysonParser("{\"fst\": 5,:12,\"last\":45}").valid());
	}
	
	@Test
	public void testInvalidJSONObjectMissingValue() {
		ValidationHandler handler = new ValidationHandler();
		new LysonParser("{\"fst\": 5,\"snd\":,\"last\":45}").parse(handler);
		assertFalse(handler.valid());
		assertEquals(LysonParsingException.class, handler.cause().getClass());
	}

	@Test
	public void testValidJSON() {
		assertTrue(new LysonParser("[8,{\"fst\": 5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\",\"frth\":{\"last\":45}}]").valid());
	}
		
	@Test
	public void testInvalidJSON() {
		assertFalse(new LysonParser("[8,{\"fst\": 5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\",{\"last\":45}}]").valid());
	}
}
