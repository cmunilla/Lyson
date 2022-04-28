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
	
	private Deque<Nested> nesteds;
	private StringBuilder builder;

	private String currentPath;
	
	private final String target;
	private String[] targetElements;
	
	private final boolean wildcard;
	
	/**
	 * Constructor
	 * 
	 * Instantiates a new EvaluationContext
	 * 
	 * @param target
	 */
	public EvaluationContext(String target) {
		String formatedTarget = target;
		if(target.startsWith("/"))
			formatedTarget = formatedTarget.substring(1);
		if(target.endsWith("/"))
			formatedTarget = formatedTarget.substring(0,formatedTarget.length()-1);
		this.target = formatedTarget;

		this.targetElements = this.target.split("/");
		this.wildcard  = EvaluationHandler.WILDCARD.equals(this.targetElements[this.targetElements.length-1]);
		this.builder = new StringBuilder();
		this.nesteds = new LinkedList<>();
	}

	/**
	 * @return the currentPath
	 */
	public String getCurrentPath() {
		return currentPath;
	}

	/**
	 * @param currentPath the currentPath to set
	 */
	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}
	
	/**
	 * @return the String target path of this EvaluationContext
	 */
	public String getTargetPath() {
		return this.target;
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
	 * Returns the JSON formated String content of the StringBuilder of 
	 * this EvaluationContext
	 * 
	 * @return this EvaluationContext's JSON formated String content 
	 */
	public String getCollected() {
		return this.builder.toString();
	}

	/**
	 * @return the size of the collected String
	 */
	public int getCollectedLength() {
		return this.builder.length();
	}
	
	/**
	 * Clear the StringBuilder of this EvaluationContext
	 */
	public void reset() {
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
		String formatedPath = event.getPath();			
		if(event.getPath().startsWith("/"))
			formatedPath = formatedPath.substring(1);
		if(event.getPath().endsWith("/") && event.getPath().length() > 1)
			formatedPath = formatedPath.substring(0,formatedPath.length()-1);
		
		String[] _pathElements = null;
		int length = 0;
		
		this.setCurrentPath(formatedPath);
		
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
		return (i == length && length == this.targetElements.length)
			?(_pathElements.length==this.targetElements.length?2:1):0;
	}
	
	/**
	 * @return the wildcard
	 */
	public boolean isWildcard() {
		return wildcard;
	}
}
