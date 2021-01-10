/*
 * MIT License
 *
 * Copyright (c) 2019 - 2021  Christophe Munilla
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
package cmssi.lyson.event;

/**
 * JSON parsing event
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.5
 */
public interface ParsingEvent {
	
	/**
	 * JSON Object opening event type constant
	 */
	public static final int JSON_OBJECT_OPENING = 1;
	/**
	 * JSON Object closing event type constant
	 */
	public static final int JSON_OBJECT_CLOSING = 2;
	/**
	 * JSON Object item event type constant
	 */
	public static final int JSON_OBJECT_ITEM = 4;
	/**
	 * JSON Array opening event type constant
	 */
	public static final int JSON_ARRAY_OPENING = 8;
	/**
	 * JSON Array closing event type constant
	 */
	public static final int JSON_ARRAY_CLOSING = 16;
	/**
	 * JSON Array item event type constant
	 */
	public static final int JSON_ARRAY_ITEM = 32;

	/**
	 * Returns the event type constant of this ParsingEvent
	 * 
	 * @return this ParsingEvent type
	 */
	int getType();
	
	/**
	 * Returns the String path of this ParsingEvent
	 * 
	 * @return this ParsingEvent path
	 */
	String getPath();
	
	/**
	 * Defines the String path of this ParsingEvent
	 * 
	 * @param path the String path of this ParsingEvent
	 * @return this ParsingEvent
	 */
	ParsingEvent withPath(String path);
	
	/**
	 * Returns the &lt;P&gt; extended ParsingEvent type adapted 
	 * from this one
	 * 
	 * @param <P> the target extended PasingEvent type
	 * @param type the targeted &lt;P&gt; extended ParsingEvent 
	 * type
	 * 
	 * @return the extended ParsingEvent type adapted from this
	 *  one
	 */
	<P extends ParsingEvent> P adapt(Class<P> type);
}
