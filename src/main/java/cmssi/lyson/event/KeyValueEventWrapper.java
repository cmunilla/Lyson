/*
 * MIT License
 *
 * Copyright (c) 2019 Christophe Munilla
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
 * A KeyValueEventWrapper wraps a {@link ParsingEvent} and implements the {@link KeyValueEvent}
 * interface
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.3
 */
public class KeyValueEventWrapper extends ValuableEventWrapper implements KeyValueEvent {

	private String key;

	/**
	 * Constructor
	 * 
	 * @param event the {@link ParsingEvent} to be wrapped
	 */
	public KeyValueEventWrapper(ParsingEvent event){
		super(event);
	}
	
	@Override
	public KeyValueEventWrapper withKey(String key) {
		this.key = key;
		return this;
	}

	@Override
	public KeyValueEventWrapper withValue(Object value) {
		super.withValue(value);
		return this;
	}
	
	@Override
	public String getKey(){
		return this.key;
	}
}
