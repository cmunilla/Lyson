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
 * JSON ParsingEvent dedicated to array opening one
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.2
 */
public interface ArrayOpeningEvent  {

	/**
	 * Returns the current Integer index of the JSON array
	 * whose opening was delimited by this ArrayOpeningEvent
	 * 
	 * @return the associated JSON array current Integer index
	 */
	int getInnerIndex();

	/**
	 * Defines the current Integer index of the JSON array
	 * whose opening was delimited by this ArrayOpeningEvent
	 * 
	 * @param innerIndex the associated JSON array current 
	 * Integer index
	 * 
	 * @return this ArrayOpeningEvent
	 */
	ArrayOpeningEvent withInnerIndex(int innerIndex) ;
	
}
