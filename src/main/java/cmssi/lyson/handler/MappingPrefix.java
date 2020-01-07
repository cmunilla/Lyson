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

import cmssi.lyson.annotation.LysonMapping;

/**
 * Hold the String prefix to be used in a mapping process
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.3
 */
public class MappingPrefix {
	
	private String[] prefix = null;
	
	/**
	 * Constructor 
	 * 
	 * @param mappedType the type of the mapped value Object(s) 
	 * allowing to create the String prefix of the MappingPrefix 
	 * to be instantiated 
	 */
	public MappingPrefix(Class<?> mappedType){
		this.findPrefix(mappedType);
	}

	/**
	 * Constructor
	 */
	public MappingPrefix(){}
	
	/**
	 * Returns true this MappingPrefix's String prefix is not null -
	 * Otherwise returns false 
	 * 
	 * @return true if the String prefix exists - false otherwise
	 */
	boolean exists() {
		return this.prefix!=null;
	}
		
    /**
     * Returns the suffix of the String path passed as parameter if the 
     * String prefix of this MappingPrefix is recognized as effectively
     * prefixing it - Otherwise returns the path passed as parameter as 
     * is
     *  
     * @param path the String path to remove the prefix of
     * 
     * @return the suffix of the specified String path if it
     * starts with this MappingPrefix's String prefix - otherwise
     * returns the specified path as is 
     */
    protected String getSuffix(String path) {
		if(path != null && this.prefix != null) {	
			String[] pathElements = path.split("/");
			int pathElementsLength = pathElements.length;
			if((pathElementsLength - 1) < this.prefix.length) {
				return path;
			}
			int ind = 0;
			int pos = 0;
			for(;ind < this.prefix.length;ind++) {
				if(prefix[ind].equals("*")|| prefix[ind].equals(pathElements[ind+1])) {
					pos+=pathElements[ind+1].length();
					pos+=1;
					continue;
				}
				break;
			}
			return path.substring(pos);
		}
		return path;
	}
	
    /**
     * Returns true if the String path passed as parameter is equal to
     * this MappingPrefix's prefix - Otherwise returns false
     *  
     * @param path the String path to evaluate the prefix equality to
     * 
     * @return true if the specified path is equals to the defined prefix -
     * false otherwise 
     */
	protected boolean isPrefix(String path) {	
		if(path != null && this.prefix != null) {
			String[] pathElements = path.split("/");
			int pathElementsLength = pathElements.length;
			if((pathElementsLength - 1) != this.prefix.length) {
				return false;
			}
			int ind = 0;
			for(;ind < this.prefix.length;ind++) {
				if(prefix[ind].equals("*")|| prefix[ind].equals(pathElements[ind+1])) {
					continue;
				}
				break;
			}
			return (ind == this.prefix.length);
		}
		return false;
	}
		
	// Identify the mapping value of the @LysonMapping annotation 
	// annotating the mappedType argument if any, and use it to create
	// the components of the String prefix to be used while the mapping
	// process
	private void findPrefix(Class<?> mappedType) {
		if(mappedType == null) {
			return;
		}
		LysonMapping lma = mappedType.getAnnotation(LysonMapping.class);
		if(lma != null) {
			String typeMapping = lma.mapping();
			if(typeMapping != null) {
				String[] typeMappingElements = typeMapping.split("/");
				int mappingElementsLength = typeMappingElements.length;
				if(mappingElementsLength - 1 > 0) {
					this.prefix = new String[(mappingElementsLength - 1)];
					System.arraycopy(typeMappingElements, 1, this.prefix, 0, (mappingElementsLength - 1));
				}
			}
	    }
	}
}
