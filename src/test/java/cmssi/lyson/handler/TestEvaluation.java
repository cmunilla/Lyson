package cmssi.lyson.handler;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import cmssi.lyson.LysonParser;
import cmssi.lyson.handler.evaluation.DefaultEvaluationCallback;
import cmssi.lyson.handler.evaluation.EvaluationCallback;
import cmssi.lyson.handler.evaluation.EvaluationContext;
import cmssi.lyson.handler.evaluation.EvaluationHandler;
import cmssi.lyson.handler.evaluation.EvaluationResult;
import cmssi.lyson.handler.evaluation.predicate.Expression;
import cmssi.lyson.handler.evaluation.predicate.LogicalOperator;
import cmssi.lyson.handler.evaluation.predicate.ValidationTime;
import cmssi.lyson.handler.evaluation.predicate.Verifiable;

public class TestEvaluation {
	
	@Mock
	Verifiable verifiable;
	
	
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
	

	@Test
	public void testEmptyExpression() {
		Expression expression = new Expression(LogicalOperator.NOP);
		assertTrue(expression.verified(ValidationTime.EARLY));		
		assertTrue(expression.verified(ValidationTime.LATELY));
	}
	
	@Test
	public void testExpressionWithOneVerifiable() {
		Verifiable verifiable = Mockito.mock(Verifiable.class);	
		Mockito.when(verifiable.verified(Mockito.eq(ValidationTime.EARLY))).thenReturn(true);
		Mockito.when(verifiable.verified(Mockito.eq(ValidationTime.LATELY))).thenReturn(false);
		
		EvaluationContext context = Mockito.mock(EvaluationContext.class);		
		
		Expression expression = new Expression(LogicalOperator.NOP);
		expression.addVerifiable(verifiable);		
		assertTrue(expression.verified(ValidationTime.EARLY));		
		expression.verify(context);		
		assertFalse(expression.verified(ValidationTime.LATELY));
	}

	@Test
	public void testExpressionWithOneVerifiableAndNullLogicalOperator() {
		Verifiable verifiable = Mockito.mock(Verifiable.class);	
		Mockito.when(verifiable.verified(Mockito.eq(ValidationTime.EARLY))).thenReturn(true);
		Mockito.when(verifiable.verified(Mockito.eq(ValidationTime.LATELY))).thenReturn(false);
		
		EvaluationContext context = Mockito.mock(EvaluationContext.class);		
		
		Expression expression = new Expression(null);
		expression.addVerifiable(verifiable);		
		assertTrue(expression.verified(ValidationTime.EARLY));		
		expression.verify(context);		
		assertFalse(expression.verified(ValidationTime.LATELY));
	}
	

	@Test
	public void testExpressionWithTwoVerifiablesAndANDLogicalOperator() {
		Verifiable verifiableOne = Mockito.mock(Verifiable.class);	
		Mockito.when(verifiableOne.verified(Mockito.eq(ValidationTime.EARLY))).thenReturn(true);
		Mockito.when(verifiableOne.verified(Mockito.eq(ValidationTime.LATELY))).thenReturn(false);

		Verifiable verifiableTwo = Mockito.mock(Verifiable.class);	
		Mockito.when(verifiableTwo.verified(Mockito.any(ValidationTime.class))).thenReturn(true);
		
		EvaluationContext context = Mockito.mock(EvaluationContext.class);		
		
		Expression expression = new Expression(LogicalOperator.AND, Arrays.asList(verifiableOne,verifiableTwo));
		assertTrue(expression.verified(ValidationTime.EARLY));		
		expression.verify(context);		
		assertFalse(expression.verified(ValidationTime.LATELY));
	}


	@Test
	public void testExpressionWithTwoVerifiablesAndORLogicalOperator() {
		Verifiable verifiableOne = Mockito.mock(Verifiable.class);	
		Mockito.when(verifiableOne.verified(Mockito.eq(ValidationTime.EARLY))).thenReturn(true);
		Mockito.when(verifiableOne.verified(Mockito.eq(ValidationTime.LATELY))).thenReturn(false);

		Verifiable verifiableTwo = Mockito.mock(Verifiable.class);	
		Mockito.when(verifiableTwo.verified(Mockito.any(ValidationTime.class))).thenReturn(true);
		
		EvaluationContext context = Mockito.mock(EvaluationContext.class);		
		
		Expression expression = new Expression(LogicalOperator.OR, Arrays.asList(verifiableOne,verifiableTwo));
		assertTrue(expression.verified(ValidationTime.EARLY));		
		expression.verify(context);		
		assertTrue(expression.verified(ValidationTime.LATELY));
	}
	
	@Test
	public void testExpressionWithTwoVerifiablesAndXORLogicalOperator() {
		Verifiable verifiableOne = Mockito.mock(Verifiable.class);	
		Mockito.when(verifiableOne.verified(Mockito.eq(ValidationTime.EARLY))).thenReturn(true);
		Mockito.when(verifiableOne.verified(Mockito.eq(ValidationTime.LATELY))).thenReturn(false);

		Verifiable verifiableTwo = Mockito.mock(Verifiable.class);	
		Mockito.when(verifiableTwo.verified(Mockito.any(ValidationTime.class))).thenReturn(true);
		
		EvaluationContext context = Mockito.mock(EvaluationContext.class);		
		
		Expression expression = new Expression(LogicalOperator.XOR, Arrays.asList(verifiableOne,verifiableTwo));
		assertFalse(expression.verified(ValidationTime.EARLY));		
		expression.verify(context);		
		assertTrue(expression.verified(ValidationTime.LATELY));
	}
}
