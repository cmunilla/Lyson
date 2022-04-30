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

import java.util.Deque;
import java.util.LinkedList;

import cmssi.lyson.event.ParsingEvent;

/**
 * An EvaluationContext wraps information about an running evaluation process
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class EvaluationContext {

	public static final int NO_MATCH = 0;
	public static final int PARTIAL_MATCHING = 1;
	public static final int COMPLETE_MATCHING = 2;
	
	private Deque<Nested> nesteds;
	private StringBuilder builder;

	private String matchingPath;
	
	private final String target;
	private String[] targetElements;
	
	private final boolean wildcard;
		
	/**
	 * Constructor
	 * 
	 * Instantiates a new EvaluationContext
	 * 
	 * @param evaluation the {@link Evaluation} on which to base the EvaluationContext 
	 * to be instantiated
	 */
	public EvaluationContext(Evaluation evaluation) {
		this.target = evaluation.getPath();
		this.targetElements = this.target.split("/");
		this.wildcard  = EvaluationHandler.WILDCARD.equals(this.targetElements[this.targetElements.length-1]);
		this.builder = new StringBuilder();
		this.nesteds = new LinkedList<>();
	}

	
	/**
	 * Returns the String target path of this EvaluationContext
	 * 
	 * @return this EvaluationContext's target String path
	 */
	public String getTargetPath() {
		return this.target;
	}
	
	/**
	 * Returns the String path of a matching {@link ParsingEvent} if any; returns null otherwise
	 *  
	 * @return the String path of a matching {@link ParsingEvent}; null otherwise
	 */
	public String getMatchingPath() {
		return matchingPath;
	}

	/**
	 * @return the nested
	 */
	public Nested getNested() {
		if(nesteds.isEmpty())
			return null;
		return nesteds.peekLast();
	}

	/**
	 * @param jsonEntity the JsonEntity to wrap into a new Nested instance
	 */
	public void addNested(JsonEntity jsonEntity) {
		int pos = 0;
		if(!this.nesteds.isEmpty())
			pos = this.nesteds.getLast().index();
		this.nesteds.addLast(new Nested(jsonEntity,pos));
	}

	/**
	 * @return the removed Nested instance if it exists
	 */
	public Nested removeNested() {
		if(nesteds.isEmpty())
			return null;
		return this.nesteds.removeLast();
	}	
	
	/**
	 * Appends the JSON formated String passed as parameter to the StringBuilder 
	 * of this EvaluationContext
	 * 
	 * @param json the JSON formated String to append to this EvaluationContext's
	 * StringBuilder
	 */
	public void collect(String json) {
		this.builder.append(json);
	}
	
	/**
	 * Returns the JSON formated String collected by this EvaluationContext
	 * 
	 * @return this EvaluationContext's JSON formated String content 
	 */
	public String getCollected() {
		return this.builder.toString();
	}
	
	/**
	 * Clear the JSON formated String collected by this EvaluationContext
	 */
	public void clearCollected() {
		this.builder = new StringBuilder();
	}

	/**
	 * Returns the compliance level of the {@link ParsingEvent} passed as parameter with 
	 * this EvaluationContext
	 *  
	 * @param event the {@link ParsingEvent} to define the level of compliance of
	 * 
	 * @return the integer compliance level
	 */
	public int getComplianceLevel(ParsingEvent event) {
		
		this.matchingPath = null;
		
		String formatedPath = event.getPath();			
		if(event.getPath().startsWith("/"))
			formatedPath = formatedPath.substring(1);
		if(event.getPath().endsWith("/") && event.getPath().length() > 1)
			formatedPath = formatedPath.substring(0,formatedPath.length()-1);
		
		String[] _pathElements = null;
		int length = 0;
				
		if(formatedPath.length() > 0) {
			_pathElements = formatedPath.split("/");
			length = (_pathElements.length < this.targetElements.length)
				?_pathElements.length:this.targetElements.length;		
		} else {
			_pathElements = new String[0];
			length = 0;
		}
		
		int i = 0;			
		for(;i < length ;) {
			String pathElement = this.targetElements[i];
			String _pathElement = _pathElements[i];
			if(EvaluationHandler.WILDCARD.equals(pathElement) || _pathElement.equals(pathElement)){
				i+=1;
				continue;
			}
			break;
		}
		//if the target path and event path are equivalent (same element or wildcard target) two at two until the 
		//last element of the smallest path the compliance level is PARTIAL_MATCHING
		int level =  (i == length && length == this.targetElements.length)?PARTIAL_MATCHING:NO_MATCH;
		//if the compliance level is already PARTIAL_MATCHING it becomes COMPLETE_MATCHING if both the target
		//path and the event path are of the same length
		level+=(level==PARTIAL_MATCHING && _pathElements.length==this.targetElements.length?1:0);
		
		if(level == COMPLETE_MATCHING)			
			this.matchingPath = formatedPath;
		
		return level;
	}
	
	/**
	 * @return the wildcard
	 */
	public boolean isWildcard() {
		return wildcard;
	}
	
	/**
	 * @return true if no data has been collected ; false otherwise
	 */
	public boolean empty() {
		return this.builder.length()==0;
	}
}
