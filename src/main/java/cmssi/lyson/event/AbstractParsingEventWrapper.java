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

public abstract class AbstractParsingEventWrapper implements ParsingEvent, ParsingEventWrapper {

	protected final ParsingEvent event;

	/**
	 * Constructor
	 * 
	 * @param event the {@link ParsingEvent} to be wrapped
	 */
	public AbstractParsingEventWrapper(ParsingEvent event){
		this.event = event;
	}
	
	@Override
	public ParsingEvent getEvent() {
		return this.event;
	}
	
	@Override
	public int getType() {
		return this.event.getType();
	}

	@Override
	public String getPath() {
		return this.event.getPath();
	}

	@Override
	public ParsingEvent withPath(String path) {
		this.event.withPath(path);
		return this;
	}
	
	@Override
	public <P extends ParsingEvent> P adapt(Class<P> type){
		if(type.isAssignableFrom(getClass())) {
			return (P)this;
		}
		if(this.event != null) {
			return this.event.adapt(type);
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.event.toString());
		return builder.toString();
	}
}
