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

import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.event.KeyValueEventWrapper;
import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.event.ValuableEventWrapper;
import cmssi.lyson.exception.LysonParsingException;
import cmssi.lyson.handler.LysonParserHandler;


/**
 *  An EvaluationProcessor is in charge of extracting element(s) of a parsed json document according to a
 *  specified path parameter
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class EvaluationProcessor implements LysonParserHandler {
	
	private static final Logger LOG = Logger.getLogger(EvaluationProcessor.class.getCanonicalName());

	private EvaluationContext context;
	private boolean complete = false;
	
	/**
	 * Constructor
	 * 
	 * Instantiates a new EvaluationProcessor
	 * 
	 * @param path the String path parameterizing the process of the 
	 * EvaluationProcessor to be instantiated
	 */
	public EvaluationProcessor(String path){
		this.context = new EvaluationContext(path);
	}
	
	/**
	 * Returns true if the path of this EvaluationProcessor matched an element of the parsed json document ;
	 * otherwise returns false
	 * 
	 * @return true if this EvaluationProcessor's path matched an element of the parsed json document; 
	 * false otherwise 
	 */
	public boolean isComplete() {
		return this.complete;
	}

	/**
	 * Returns the path used to parameterize this EvaluationProcessor's processing
	 * 
	 * @return the path the path of this EvaluationProcessor
	 */
	public String getPath() {
		return context.getTargetPath();
	}
	
	/**
	 * Returns the {@link EvaluationResult} resulting from the parsed json document processing
	 * 
	 * @return the processed {@link EvaluationResult}
	 */
	public EvaluationResult getEvaluationResult() {
		EvaluationResult result = new EvaluationResult(context.getTargetPath(), context.getCurrentPath(), this.context.getCollected());
		if(context.isWildcard()) {
			this.complete = false;
			this.context.reset();
		}
		return result;
	}
	
	@Override
	public boolean handle(ParsingEvent event){		
		
		if(event == null)
			this.complete = true;
		
		if(this.complete)
			return false;
		
		int compliance = this.context.getComplianceLevel(event);
		
		if(compliance > 0){
			
			boolean opening = false;
			boolean closing = false;
			
			switch(event.getType()) {
				case ParsingEvent.JSON_ARRAY_OPENING:
					opening = true;
					int pos = 0;
					Nested nested = this.context.getNested();
					if(nested!=null) {
						pos = nested.inc();
						if(pos > 0 && this.context.getCollectedLength() > 0)
							this.context.collect(",");
						if(nested.jsonEntity().equals(JsonEntity.OBJECT)) {
							KeyValueEventWrapper vwrapper = event.adapt(KeyValueEventWrapper.class);
							this.context.collect("\"");
							this.context.collect(vwrapper.getKey());
							this.context.collect("\":");							
						}
					}
					this.context.collect("[");
					this.context.addNested(JsonEntity.ARRAY);
					break;
				case ParsingEvent.JSON_ARRAY_ITEM:
					nested = this.context.getNested();
					if(nested!=null) {
						pos = nested.inc();
						if(pos > 0 && this.context.getCollectedLength() > 0)
							this.context.collect(",");
					}
					ValuableEventWrapper vwrapper = event.adapt(ValuableEventWrapper.class);
					Object eventValue = vwrapper==null?null:vwrapper.getValue();
					String value = null;					
					if(eventValue!=null && eventValue.getClass() == String.class)
						value = String.format("\"%s\"", ((String)eventValue).replace("\"", "\\\""));
					else
						value = String.valueOf(eventValue);
					this.context.collect(value);
					break;
				case ParsingEvent.JSON_ARRAY_CLOSING:
					closing = true;
					this.context.collect("]");
					this.context.removeNested();
					break;
				case ParsingEvent.JSON_OBJECT_OPENING:
					opening = true;
					nested = this.context.getNested();
					pos = 0;
					if(nested!=null) {
						KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
						pos = nested.inc();
						if(pos > 0 && this.context.getCollectedLength() > 0)
							this.context.collect(",");						
						if(nested.jsonEntity().equals(JsonEntity.OBJECT)) {
							this.context.collect("\"");
							this.context.collect(kvwrapper.getKey());
							this.context.collect("\":");
						}
					}					
					this.context.collect("{");
					this.context.addNested(JsonEntity.OBJECT);
					break;
				case ParsingEvent.JSON_OBJECT_ITEM:
					nested = this.context.getNested();
					pos = 0;
					if(nested!=null) {
						pos = nested.inc();
						if(pos > 0  && this.context.getCollectedLength() > 0)
							this.context.collect(",");
						KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
						this.context.collect("\"");
						this.context.collect(kvwrapper.getKey());
						this.context.collect("\"");
						this.context.collect(":");
					}
					KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
					eventValue = kvwrapper==null?null:kvwrapper.getValue();
					if(eventValue!=null && eventValue.getClass() == String.class)
						value = String.format("\"%s\"", ((String)eventValue).replace("\"", "\\\""));
					else
						value = String.valueOf(eventValue);
					this.context.collect(value);
					break;
				case ParsingEvent.JSON_OBJECT_CLOSING:
					closing = true;
					this.context.collect("}");
					this.context.removeNested();
					break;
				default:
					break;
			}
			if(this.context.getCollectedLength() > 0 && compliance > 1 &&  
			  (closing || (context.isWildcard() && !opening) || (!context.isWildcard() && !opening && !closing)))
					this.complete = true;
		}
		return true;
	}

	@Override
	public void handle(LysonParsingException parsingException) {
		if(LOG.isLoggable(Level.SEVERE)) 
			LOG.log(Level.SEVERE, parsingException.getMessage(), parsingException);
	}

}