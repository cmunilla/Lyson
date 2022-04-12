/*
 * MIT License
 *
 * Copyright (c) 2019 - 2022  Christophe Munilla
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cmssi.lyson.handler.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import cmssi.lyson.LysonParser;
import cmssi.lyson.LysonParserHandlerCallable;
import cmssi.lyson.LysonParserHandlerThreadExecutor;
import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.exception.LysonParsingException;
import cmssi.lyson.handler.LysonParserHandler;


/**
 * EvaluationHandler allows to extract parts of a json document using a set of String paths
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class EvaluationHandler implements LysonParserHandler {
	
	private static final Logger LOG = Logger.getLogger(EvaluationHandler.class.getCanonicalName());

	static final String WILDCARD = "*";
	
	public static final EvaluationResult END_OF_PARSING = new EvaluationResult(null,null); 

    private List<EvaluationResult> extractions = null;	
	private List<LysonParserHandlerCallable> callables = null ;
	private List<EvaluationProcessor> processors = null ;
	
	private EvaluationCallback callback = null;

    private final LysonParserHandlerThreadExecutor executor;

    /**
     * Constructor
     * 
     * Instantiate a new EvaluationHandler
     * 
     * @param paths the List of String paths for which to extract json elements
     */
    public EvaluationHandler(List<String> paths) {
	    this(paths, null);	
    }
    
    /**
     * Constructor
     * 
     * Instantiate a new EvaluationHandler
     * 
     * @param paths the List of String paths for which to extract json elements
     * @param callback the {@link EvaluationCallback} to be notified of new {@link EvaluationResult} results
     */
    public EvaluationHandler(List<String> paths, EvaluationCallback callback) {
		int length = paths==null?0:paths.size();
		if(length > LysonParser.MAX_THREAD) 
			length = LysonParser.MAX_THREAD;
		
		this.executor = new LysonParserHandlerThreadExecutor(length);

		if(length == 0)
			return;
		
		this.processors = paths.stream(
				).map(p -> new EvaluationProcessor(p)
				).collect(Collectors.toList());
		this.callables = this.processors.stream(
				).map(p -> new LysonParserHandlerCallable(p)
				).collect(Collectors.toList());
		
		if(callback!=null)
			this.callback = callback;
		else
			this.extractions = new ArrayList<>();	
    }

	/**
	 * Returns the List of built {@link ExtractionResult}s
	 * 
	 * @return the List of {@link ExtractionResult} results
	 */
	public List<EvaluationResult> getResults() {	
		return this.extractions;
	}
	
	@Override
	public boolean handle(ParsingEvent event) {		
		try {
			if(event == null) {
				if(this.callback!=null)
					this.callback.handle(END_OF_PARSING);
				this.callables.clear();
				this.executor.shutdownNow();
				return false;
			}
			executor.invokeAll(callables, event);
            for(int i=0; i< processors.size();) {
				EvaluationProcessor processor = processors.get(i);
				if(processor.isComplete()) {
					if(callback!=null)
						callback.handle(processor.getEvaluation());	
					else {
						if(!processor.getPath().endsWith(WILDCARD))					
							processors.remove(i);
						extractions.add(processor.getEvaluation());
					}
				} else
					i+=1;
			}
			if(processors.isEmpty())
				return false;
			
			return true;
			
        } catch (LysonParsingException e) {        	
        	processors.forEach(h -> { 
        		h.handle(e); 
        	});
    		if(LOG.isLoggable(Level.SEVERE)) 
    			LOG.log(Level.SEVERE,e.getMessage(),e);    		
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
		}
		return false;
	}	

	@Override
	public void handle(LysonParsingException parsingException) {
		if(LOG.isLoggable(Level.SEVERE)) 
			LOG.log(Level.SEVERE, parsingException.getMessage(), parsingException);
	}

//	public static void main(String args[]) {
//		
//		String s = "{\"key0\":null,\"key1\":[\"machin\",\"chose\",2],"
//			+ "\"key2\":{\"key20\":\"truc\",\"key21\":45},"
//			+ "\"key3\":{\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}}";
//		
//		EvaluationHandler handler =  new EvaluationHandler(Arrays.asList(
//			"*",
//			"/key3/key30/[3]/key301",
//			"/key3/key30/[0]",
//			"/key3",
//			"/key3/key30/[2]",
//			"/key2/key20",
//			"/key2/key25",
//			"/key3/key30/[3]/key301/*")
//		);
//		LysonParser parser = new LysonParser(s);
//		parser.parse(handler);
//		List<EvaluationResult> extractions = handler.getResults();		
//		extractions.stream().forEach(e->{
//			System.out.println( e.path+ " : " + e.result);
//		});	
//
//		System.out.println();
//		System.out.println("##########################################################");
//		System.out.println();
//		
//		EvaluationCallback callback = new EvaluationCallback() {
//			@Override
//			public void handle(EvaluationResult e) {
//				System.out.println("--------------------------------------------------------");
//				if(e == END_OF_PARSING)
//					System.out.println( "end of parsing");
//				else
//					System.out.println( e.path+ " : " + e.result);
//				System.out.println("--------------------------------------------------------");
//			}
//		};
//		
//		handler =  new EvaluationHandler(Arrays.asList(
//			"key3/key30/[3]/key301/*", 
//			"/key3/key30/*", 
//			"/key3/key30/[0]/*", 
//			"/key3/*"), 
//		callback);
//		
//		parser = new LysonParser(s);
//		parser.parse(handler);
//		System.out.println();
//		System.out.println("##########################################################");
//		System.out.println();
//		
//		s = "[null,[\"machin\",\"chose\",2],{\"key20\":\"truc\",\"key21\":45},"
//		+ "{\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}]";
//			
//		handler =  new EvaluationHandler(Arrays.asList("*"), callback);		
//		parser = new LysonParser(s);
//		parser.parse(handler);
//	}

}
