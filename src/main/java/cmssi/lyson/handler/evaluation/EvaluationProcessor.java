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

import java.util.Stack;
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

	private static enum Nested {
		OBJECT,
		ARRAY;
	}
	
	private String path = null;
	private String[] pathElements = null;
	private StringBuilder builder = null;
	private int lastIndex = -1;
	private boolean complete = false;
	private boolean isWildcard = false;
	
	private Stack<Nested> nesteds =new Stack<>();
	private Stack<Integer> indexes = new Stack<>();
	
	/**
	 * Constructor
	 * 
	 * Instantiates a new EvaluationProcessor
	 * 
	 * @param path the String path parameterizing the process of the 
	 * EvaluationProcessor to be instantiated
	 */
	public EvaluationProcessor(String path){
		String formatedPath = path;
		if(path.startsWith("/"))
			formatedPath = formatedPath.substring(1);
		if(path.endsWith("/"))
			formatedPath = formatedPath.substring(0,formatedPath.length()-1);
		this.path = formatedPath;
		this.pathElements = formatedPath.split("/");
		this.lastIndex = this.pathElements.length-1;
		this.isWildcard  = EvaluationHandler.WILDCARD.equals(this.pathElements[this.lastIndex]);
		this.builder = new StringBuilder();
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
		return path;
	}
	
	/**
	 * Returns the {@link EvaluationResult} resulting from the parsed json document processing
	 * 
	 * @return the processed {@link EvaluationResult}
	 */
	public EvaluationResult getEvaluation() {
		EvaluationResult extraction = new EvaluationResult(path, this.builder.toString());
		if(EvaluationHandler.WILDCARD.equals(this.pathElements[this.lastIndex])) {
			this.complete = false;
			this.builder = new StringBuilder();
		}
		return extraction;
	}
	
	@Override
	public boolean handle(ParsingEvent event){		
		
		if(event == null)
			this.complete = true;
		
		if(this.complete)
			return false;
		
		String formatedPath = event.getPath();			
		if(event.getPath().startsWith("/"))
			formatedPath = formatedPath.substring(1);
		if(event.getPath().endsWith("/"))
			formatedPath = formatedPath.substring(0,formatedPath.length()-1);
		
		String[] _pathElements = formatedPath.split("/");
		int length = (_pathElements.length < this.pathElements.length)
				?_pathElements.length:this.pathElements.length;			
		
		int i = 0;			
		for(;i < length ;) {
			String pathElement = this.pathElements[i];
			String _pathElement = _pathElements[i];
			if(EvaluationHandler.WILDCARD.equals(pathElement) || _pathElement.equals(pathElement)){
				i+=1;
				continue;
			}
			break;
		}
		if(i == length && length == this.pathElements.length) {
			
			boolean opening = false;
			boolean closing = false;
			
			switch(event.getType()) {
				case ParsingEvent.JSON_ARRAY_OPENING:
					opening = true;
					if(!indexes.isEmpty() && nesteds.peek().equals(Nested.OBJECT)) {
						KeyValueEventWrapper vwrapper = event.adapt(KeyValueEventWrapper.class);
						int objectIndex = this.indexes.pop().intValue();
						objectIndex+=1;
						this.indexes.push(Integer.valueOf(objectIndex));
						if(objectIndex > 0) 
							this.builder.append(",");
						this.builder.append("\"");
						this.builder.append(vwrapper.getKey());
						this.builder.append("\":");
					} else if(!indexes.isEmpty() && nesteds.peek().equals(Nested.ARRAY)) {
						int arrayIndex = this.indexes.pop().intValue();
						arrayIndex+=1;
						this.indexes.push(Integer.valueOf(arrayIndex));
						if(arrayIndex > 0) 
							this.builder.append(",");
					}
					this.builder.append("[");
					this.nesteds.push(Nested.ARRAY);
					this.indexes.push(Integer.valueOf(-1));
					break;
				case ParsingEvent.JSON_ARRAY_ITEM:
					if(!indexes.isEmpty() && nesteds.peek().equals(Nested.ARRAY)) {
						int arrayIndex = this.indexes.pop().intValue();
						arrayIndex+=1;
						this.indexes.push(Integer.valueOf(arrayIndex));
						if(arrayIndex > 0 && builder.length() > 0)
							this.builder.append(",");
					}
					ValuableEventWrapper vwrapper = event.adapt(ValuableEventWrapper.class);
					Object eventValue = vwrapper==null?null:vwrapper.getValue();
					String value = null;					
					if(eventValue!=null && eventValue.getClass() == String.class)
						value = String.format("\"%s\"", ((String)eventValue).replace("\"", "\\\""));
					else
						value = String.valueOf(eventValue);
					this.builder.append(value);
					break;
				case ParsingEvent.JSON_ARRAY_CLOSING:
					closing = true;
					this.builder.append("]");
					this.indexes.pop();
					this.nesteds.pop();
					break;
				case ParsingEvent.JSON_OBJECT_OPENING:
					opening = true;
					if(!indexes.isEmpty() && nesteds.peek().equals(Nested.OBJECT)) {
						KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
						int objectIndex = this.indexes.pop().intValue();
						objectIndex+=1;
						this.indexes.push(Integer.valueOf(objectIndex));
						if(objectIndex > 0) 
							this.builder.append(",");
						this.builder.append("\"");
						this.builder.append(kvwrapper.getKey());
						this.builder.append("\":");
					} else if(!indexes.isEmpty() && nesteds.peek().equals(Nested.ARRAY)) {
						int arrayIndex = this.indexes.pop().intValue();
						arrayIndex+=1;
						this.indexes.push(Integer.valueOf(arrayIndex));
						if(arrayIndex > 0) 
							this.builder.append(",");
					}
					this.builder.append("{");
					this.nesteds.push(Nested.OBJECT);
					this.indexes.push(Integer.valueOf(-1));
					break;
				case ParsingEvent.JSON_OBJECT_ITEM:
					if(!indexes.isEmpty() && nesteds.peek().equals(Nested.OBJECT)) {
						KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
						int objectIndex = this.indexes.pop().intValue();
						objectIndex+=1;
						this.indexes.push(Integer.valueOf(objectIndex));
						if(objectIndex > 0  && builder.length() > 0)
							this.builder.append(",");
						this.builder.append("\"");
						this.builder.append(kvwrapper.getKey());
						this.builder.append("\"");
						this.builder.append(":");
					}
					KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
					eventValue = kvwrapper==null?null:kvwrapper.getValue();
					if(eventValue!=null && eventValue.getClass() == String.class)
						value = String.format("\"%s\"", ((String)eventValue).replace("\"", "\\\""));
					else
						value = String.valueOf(eventValue);
					this.builder.append(value);
					break;
				case ParsingEvent.JSON_OBJECT_CLOSING:
					closing = true;
					this.builder.append("}");
					this.indexes.pop();
					this.nesteds.pop();
					break;
				default:
					break;
			}
			if(this.pathElements.length == _pathElements.length && 
			  ((closing && this.indexes.size()==0) || (isWildcard && !opening) || (!isWildcard && !opening && !closing)))
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