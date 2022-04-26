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

import java.util.stream.Stream;

/**
 * An EvaluationCallback is the recipient of the {@link EvaluationResult}s of an 
 * evaluation process
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public interface EvaluationCallback {

	/**
	 * Treats the {@link EvaluationResult} passed as parameter
	 * 
	 * @param result the {@link EvaluationResult} to treat
	 */
	void handle(EvaluationResult result);
	
	/**
	 * Returns the Stream of registered {@link EvaluationResult}s
	 * 
	 * @return the {@link EvaluationResult}s Stream
	 */
	Stream<EvaluationResult> results();
}