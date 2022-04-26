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
import java.util.Collections;
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
 * EvaluationHandler allows to extract elements of a json document using a set of {Evaluation}s
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class EvaluationHandler implements LysonParserHandler {
	
	private static final Logger LOG = Logger.getLogger(EvaluationHandler.class.getCanonicalName());

	static final String WILDCARD = "*";
	
	public static final EvaluationResult END_OF_PARSING = new EvaluationResult(null, null, null); 

	private EvaluationCallback callback = null;
	
	private List<LysonParserHandlerCallable> callables = null ;
	private List<EvaluationProcessor> processors = null ;	

    private final LysonParserHandlerThreadExecutor executor;

    /**
     * Constructor
     * 
     * Instantiate a new EvaluationHandler
     * 
     * @param paths the List of String paths for which to extract json elements
     */
    public EvaluationHandler(List<String> paths) {
	    this(paths, new DefaultEvaluationCallback());	
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
    	
    	List<String> pth = new ArrayList<>();
    	if(paths!=null)
    		pth.addAll(paths);
    	
		int length = pth.size();
		if(length > LysonParser.MAX_THREAD) 
			length = LysonParser.MAX_THREAD;
		
		if(length > 0){
			this.executor = new LysonParserHandlerThreadExecutor(length);
			this.processors = pth.stream(
					).map(p -> new EvaluationProcessor(p)
					).collect(Collectors.toList());
			this.callables = this.processors.stream(
					).map(p -> new LysonParserHandlerCallable(p)
					).collect(Collectors.toList());
		} else {
			this.executor = new LysonParserHandlerThreadExecutor(1);
			this.processors = Collections.emptyList();
			this.callables = Collections.emptyList();
		}
		if(callback==null)
			this.callback = new DefaultEvaluationCallback();
		else
			this.callback = callback;
    }

	/**
	 * Returns the List of resulting {@link ExtractionResult}s
	 * 
	 * @return the List of {@link ExtractionResult} results
	 */
	public List<EvaluationResult> getResults() {	
		return this.callback.results().collect(Collectors.toList());
	}
	
	@Override
	public boolean handle(ParsingEvent event) {		
		try {
			if(event == null) {
				this.callback.handle(END_OF_PARSING);
				this.callables.clear();
				this.executor.shutdownNow();
				return false;
			}
			executor.invokeAll(callables, event);
			int i=0;
			int offset = 0;
			while(i< processors.size()) {
				offset = 1;
				EvaluationProcessor processor = processors.get(i);
				if(processor.isComplete()) {
					callback.handle(processor.getEvaluationResult());
					if(!processor.getPath().endsWith(WILDCARD))	{			
						processors.remove(i);
						offset = 0;
					}
				}
				i+=offset;
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
}
