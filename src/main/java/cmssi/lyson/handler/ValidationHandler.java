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

import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.exception.LysonParsingException;

/**
 * {@link LysonParserHandler} implementation dedicated to 
 * a JSON chars sequence validation
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.4
 */
public class ValidationHandler implements LysonParserHandler {

	private static final Logger LOG = Logger.getLogger(ValidationHandler.class.getName());
	
	private LysonParsingException parsingException;
	private int count;
	
	@Override
	public boolean handle(ParsingEvent event) {
		if(event == null) {
			return false;
		}
		if(LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE,event.toString());
		}
		count++;
	    return true;
	}
	
	@Override
	public void handle(LysonParsingException parsingException) {
		this.parsingException = parsingException;
	}
	
	/**
	 * Returns true if the parsed JSON formated chars sequence is 
	 * well formed  - returns false otherwise
	 * 
	 * @return
	 * <ul>
	 * 	<li>true if the parsed JSON is valid</li>
	 *  <li>false otherwise</li>
	 * </ul>
	 */
	public boolean valid() {
		return (this.parsingException == null) && count > 0;
	}
	
	/**
	 * Returns the {@link LysonParsingException} triggered while
	 * parsing the JSON formated chars sequence if it exists - otherwise
	 * returns null.
	 *  
	 * @return the triggered {@link LysonParsingException} if any
	 */
	public LysonParsingException cause() {
		return this.parsingException;		
	}
}
