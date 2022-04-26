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

/**
 * A Nested instance wraps information about a parsed JSON entity
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class Nested {

	private final JsonEntity jsonEntity;
	private final int position;
	private int index;
	
	/**
	 * Constructor
	 * 
	 * Instantiates a new Nested 
	 * 
	 * @param jsonEntity the {@link JsonEntity} wrapped by the Nested to be instantiated
	 * @param position the integer position of the JSON entity wrapped by the Nested to be 
	 * instantiated
	 */
	public Nested(JsonEntity jsonEntity, int position){
		this.jsonEntity = jsonEntity;
		this.position = position;
		this.index = -1;
	}
	
	/**
	 * Increments and returns the internal integer index of this Nested 
	 * @return the incremented integer value of this Nested's index
	 */
	public int inc(){
		this.index+=1;
		return this.index;
	}

	/**
	 * Returns the internal integer index of this Nested 
	 * @return the integer value of this Nested's index
	 */
	public int index() {
		return this.index;
	}

	/**
	 * Returns the integer position of this Nested
	 * 
	 * @return this Nested's position
	 */
	public int position() {
		return this.position;
	}
	
	/**
	 * Returns the {@link JsonEntity} wrapped by this Nested
	 * 
	 * @return this Nested's JSON entity
	 */
	public JsonEntity jsonEntity() {
		return this.jsonEntity;
	}
}