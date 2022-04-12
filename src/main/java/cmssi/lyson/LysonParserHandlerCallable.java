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
package cmssi.lyson;

import java.util.concurrent.Callable;

import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.handler.LysonParserHandler;


/**
 * Extended {@link Callable} wrapping a {@link LysonParserHandler} and a {@link ParsingEvent} to be set
 * before a call and parameterizing the resulting {@link LysonParserHandler}'s handle method invocation
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public final class LysonParserHandlerCallable implements Callable<Boolean> {
	
	private ParsingEvent parsingEvent;
	private LysonParserHandler handler;

	public LysonParserHandlerCallable(LysonParserHandler handler){
		this.handler = handler;
	}
	
	public void setParsingEvent(ParsingEvent parsingEvent) {
		this.parsingEvent = parsingEvent;
	}
	
	@Override
	public Boolean call() throws Exception {
		return this.handler.handle(this.parsingEvent);
	}
}