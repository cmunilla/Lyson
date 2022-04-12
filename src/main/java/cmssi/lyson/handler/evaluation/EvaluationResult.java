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
 * An EvaluationResult gathers the path of an evaluation process as well as its String result
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class EvaluationResult {
	
	public final String path;
	public final String result;
	
	/**
	 * Constructor
	 * 
	 * Instantiates a new EvaluationResult
	 * 
	 * @param path the String path for which the EvaluationResult is instantiated
	 * @param result String result for the specified path
	 */
	public EvaluationResult(String path, String result){
		this.path = path;
		if(result!=null && result.startsWith(","))
			this.result = result.substring(1);
		else
			this.result = result;
	}
}