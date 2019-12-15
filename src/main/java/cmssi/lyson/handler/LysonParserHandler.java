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
package cmssi.lyson.handler;

import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.exception.LysonParsingException;

/**
 * The natural recipient of the {@link ParsingEvent}s and
 * {@link LysonParsingException}s triggered by a 
 * {@link cmssi.lyson.LysonParser}
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.2
 */
public interface LysonParserHandler {
	
	/**
	 * Handles the {@link ParsingEvent} passed 
	 * as parameter
	 * 
	 * @param event the {@link ParsingEvent} to be handled
	 * 
	 * @return a boolean value defining whether to continue 
	 * or not the parsing
	 */
	boolean handle(ParsingEvent event);
	
	/**
	 * Handles the {@link LysonParsingException} passed 
	 * as parameter
	 * 
	 * @param exception the {@link LysonParsingException} to 
	 * be handled
	 */
	void handle(LysonParsingException exception);
}
