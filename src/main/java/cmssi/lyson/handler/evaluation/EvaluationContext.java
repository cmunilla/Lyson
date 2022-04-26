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

/**
 * An EvaluationContext wraps information about an running evaluation process
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class EvaluationContext {
	
	private String lastKey;
	private Object lastValue;
	
	private Deque<Nested> nesteds;
	private StringBuilder builder;
	
	private String currentPath;
	
	/**
	 * Constructor
	 * 
	 * Instantiates a new EvaluationContext
	 */
	public EvaluationContext() {
		this.builder = new StringBuilder();
		this.nesteds = new LinkedList<>();
	}
	
	/**
	 * @return the lastKey
	 */
	public String getLastKey() {
		return lastKey;
	}
	
	/**
	 * @param lastKey the lastKey to set
	 */
	public void setLastKey(String lastKey) {
		this.lastKey = lastKey;
	}
	
	/**
	 * @return the lastValue
	 */
	public Object getLastValue() {
		return lastValue;
	}
	
	/**
	 * @param lastValue the lastValue to set
	 */
	public void setLastValue(Object lastValue) {
		this.lastValue = lastValue;
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
	public void append(String json) {
		this.builder.append(json);
	}
	
	
	/**
	 * Returns the JSON formated String content of the StringBuilder of 
	 * this EvaluationContext
	 * 
	 * @return this EvaluationContext's JSON formated String content 
	 */
	public String json() {
		return this.builder.toString();
	}

	
	/**
	 * Clear the StringBuilder of this EvaluationContext
	 */
	public void reset() {
		this.builder = new StringBuilder();
	}
}
