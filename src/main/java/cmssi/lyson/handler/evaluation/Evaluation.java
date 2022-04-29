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

import cmssi.lyson.handler.evaluation.predicate.ValidationTime;
import cmssi.lyson.handler.evaluation.predicate.Verifiable;

/**
 * An Evaluation is a {@link Verifiable} and gathers the parameters allowing to launch an 
 * evaluation process :
 * <ul>
 *     <li>A targeted String path</li>
 *     <li>A Verifiable to be verified</li>
 * </ul>
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class Evaluation implements Verifiable {
	
	private String path;
	private Verifiable verifiable;
	
	/**
	 * Constructor 
	 * 
	 * Instantiate a new Evaluation
	 * 
	 * @param path the String path targeted by the Evaluation to be instantiated  
	 */
	public Evaluation (String path){		
		String formatedPath = path;
		if(path.startsWith("/"))
			formatedPath = formatedPath.substring(1);
		if(path.endsWith("/") && path.length() > 1)
			formatedPath = formatedPath.substring(0,formatedPath.length()-1);
		this.path = formatedPath;
	}
	
	/**
	 * Returns the String path targeted by this Evaluation
	 * 
	 * @return the String path of this Evaluation
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Defines the {@link Verifiable} passed as parameter as the one to be verified 
	 * by this Evaluation
	 * 
	 * @param verifiable the {@link Verifiable} to set
	 */
	public void setVerifiable(Verifiable verifiable){
		this.verifiable = verifiable;
	}

	@Override
	public void verify(EvaluationContext evaluationContext) {
		if(this.verifiable!=null)
			this.verifiable.verify(evaluationContext);
	}

	@Override
	public boolean verified(ValidationTime validationTime) {
		boolean verified = true;
		if(this.verifiable!=null)
			verified = verifiable.verified(validationTime);
		return verified;
	}
}
