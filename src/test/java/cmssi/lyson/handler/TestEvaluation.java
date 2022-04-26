package cmssi.lyson.handler;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import cmssi.lyson.LysonParser;
import cmssi.lyson.handler.evaluation.DefaultEvaluationCallback;
import cmssi.lyson.handler.evaluation.EvaluationCallback;
import cmssi.lyson.handler.evaluation.EvaluationHandler;
import cmssi.lyson.handler.evaluation.EvaluationResult;

public class TestEvaluation {
	
	@Test
	public void testJsonObjectEvaluationWithCallback() {
		
		String s = "{\"key0\":null,\"key1\":[\"machin\",\"chose\",2]," + 
			"\"key2\":{\"key20\":\"truc\",\"key21\":45}," + 
			"\"key3\":{\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}" +
		    "}";
		
		List<String> paths = Arrays.asList(
				"key3/key30/[3]/key301/*", 
				"/key3/key30/*", 
				"/key3/key30/[0]/*", 
				"/key3/*");
		
		final Set<String> expected = new HashSet<>();
		expected.add("key3/key30/[3]/key301/* : 10");
		expected.add("key3/key30/[3]/key301/* : 20");
		expected.add("key3/key30/[3]/key301/* : 11");
		expected.add("key3/key30/* : {\"key300\":\"bidule\",\"key301\":[8,2,1]}");
		expected.add("key3/key30/* : [18,\"intermediate\"]");
		expected.add("key3/key30/* : \"standalone\"");
		expected.add("key3/key30/* : {\"key300\":\"chose\",\"key301\":[10,20,11]}");
		expected.add("key3/key30/[0]/* : \"bidule\"");
		expected.add("key3/key30/[0]/* : [8,2,1]");
		expected.add("key3/* : [{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]");
		
		EvaluationCallback callback = new DefaultEvaluationCallback() {
			@Override
			public void handle(EvaluationResult e) {
				if(e != EvaluationHandler.END_OF_PARSING) {
					String result = e.target+ " : " + e.result;
					System.out.println(result);
					assert(expected.remove(result));
				}
			}
		};
		
		EvaluationHandler handler =  new EvaluationHandler(paths, callback);
		
		LysonParser parser = new LysonParser(s);
		parser.parse(handler);
		assertTrue(expected.isEmpty());
	}
	
	@Test
	public void testJsonArrayEvaluationWithCallback() {
				
		String s = "[null,[\"machin\",\"chose\",2],{\"key20\":\"truc\",\"key21\":45},"
		+ "{\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}]";

		List<String> paths = Arrays.asList("/*");
		
		final Set<String> expected = new HashSet<>();
		expected.add("* : null");
		expected.add("* : [\"machin\",\"chose\",2]");
		expected.add("* : {\"key20\":\"truc\",\"key21\":45}");
		expected.add("* : {\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}");
		
		EvaluationCallback callback = new DefaultEvaluationCallback() {
			@Override
			public void handle(EvaluationResult e) {
				if(e != EvaluationHandler.END_OF_PARSING) {
					String result = e.target+ " : " + e.result;
					System.out.println(result);
					assert(expected.remove(result));
				}
			}
		};
		
		EvaluationHandler handler =  new EvaluationHandler(paths, callback);
		
		LysonParser parser = new LysonParser(s);
		parser.parse(handler);
		assertTrue(expected.isEmpty());
	}
	
	@Test
	public void testJsonObjectEvaluationWithoutCallback() {
		String s = "{\"key0\":null,\"key1\":[\"machin\",\"chose\",2]," + 
				   "\"key2\":{\"key20\":\"truc\",\"key21\":45}," + 
				   "\"key3\":{\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}"
				   + "}";
		
		List<String> paths = Arrays.asList(
				"/*",
				"/key3/key30/[3]/key301",
				"/key3/key30/[0]",
				"/key3",
				"/key3/key30/[2]",
				"/key2/key20",
				"/key2/key25",
				"/key3/key30/[3]/key301/*");
		
		final Set<String> expected = new HashSet<>();
		expected.add("key3/key30/[3]/key301/* : 10");
		expected.add("key3/key30/[3]/key301/* : 20");
		expected.add("key3/key30/[3]/key301/* : 11");
		expected.add("key3/key30/[3]/key301 : [10,20,11]");
		expected.add("key2/key20 : \"truc\"");
		expected.add("key3/key30/[0] : {\"key300\":\"bidule\",\"key301\":[8,2,1]}");
		expected.add("key3/key30/[2] : \"standalone\"");
		expected.add("key3 : {\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}");
		expected.add("* : null");
		expected.add("* : [\"machin\",\"chose\",2]");
		expected.add("* : {\"key20\":\"truc\",\"key21\":45}");
		expected.add("* : {\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}");
		
		EvaluationHandler handler =  new EvaluationHandler(paths);
		
		LysonParser parser = new LysonParser(s);
		parser.parse(handler);
		
		List<EvaluationResult> extractions = handler.getResults();
		
		extractions.stream().forEach(e->{
			String result = e.target+ " : " + e.result;
			System.out.println(result);
			assert(expected.remove(result));
		});
		assertTrue(expected.isEmpty());	
	}
}
