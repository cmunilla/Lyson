package cmssi.lyson.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import cmssi.lyson.LysonParser;
import cmssi.lyson.event.ParsingEvent;
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
	public void testUnopenedObjectInvalidJSON() {
		assertFalse(new LysonParser("\"key\":\"value\"}").valid());
	}
	
	@Test
	public void testUnclosedObjectInvalidJSON() {
		assertFalse(new LysonParser("{\"key\":\"value\"").valid());
	}

	@Test
	public void testUnclosedArrayInvalidJSON() {
		assertFalse(new LysonParser("[\"5\",\"6\"").valid());
	}

	@Test
	public void testUnopenedArrayInvalidJSON() {
		assertFalse(new LysonParser("\"5\",\"6\"]").valid());
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
	public void testInvalidJSONObjectMissingKeySemicolon() {		
		assertFalse(new LysonParser("{\"fst\": 5,12,\"last\":45}").valid());
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

	@Test
	public void testInvalidJSONEOF() {
		assertFalse(new LysonParser("{\"key1\":\"NOT PARSED\0\"}").valid());	
	}

	@Test
	public void testNoHandlerNoParsing() {
		//nothing happen
		new LysonParser("{\"key1\":\"NOT PARSED\"}").parse();	
	}

	@Test
	public void testUserDefinedHandler() {
		/*
		
		
		 */
		
		StringBuilder expected = new StringBuilder();
		expected.append("[JSON_OBJECT_OPENING][/]\n");
		expected.append("[JSON_OBJECT_ITEM][/key1][PARSED]\n");
		expected.append("[JSON_ARRAY_OPENING ][/arr]\n");
		expected.append("[JSON_ARRAY_ITEM][/arr/[0]][5]\n");
		expected.append("[JSON_ARRAY_ITEM][/arr/[1]][69]\n");
		expected.append("[JSON_ARRAY_ITEM][/arr/[2]][È]\n");
		expected.append("[JSON_ARRAY_ITEM][/arr/[3]][l]\n");
		expected.append("[JSON_OBJECT_OPENING][/arr/[4]]\n");
		expected.append("[JSON_OBJECT_ITEM][/arr/[4]/embedded][8]\n");
		expected.append("[JSON_OBJECT_CLOSING][/arr/[4]]\n");
		expected.append("[JSON_ARRAY_CLOSING][/arr]\n");
		expected.append("[JSON_OBJECT_CLOSING][/]\n");
		final StringBuilder b = new StringBuilder();
		LysonParserHandler h = new LysonParserHandler( ) {
			@Override
			public boolean handle(ParsingEvent event) {
				b.append(event.toString());
				b.append('\n');
				return true;
			}
			@Override
			public void handle(LysonParsingException exception) {
			}			
		};		
		new LysonParser("{\"key1\":\"PARSED\",\"arr\":[5,0x45,\u00C8,\"l\",{\"embedded\":8}]}").parse(h);	
		System.out.println(b.toString());
		assertEquals(expected.toString(), b.toString());
	}
}
