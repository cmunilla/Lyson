/*
 * MIT License
 *
 * Copyright (c) 2019 - 2020  Christophe Munilla
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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapped the mapped Objects and offer an uniform access to them
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.4
 */
public class MappingWrapper<T> {
	
	private static final Logger LOG = Logger.getLogger(MappingWrapper.class.getName());
		
	private MappingConfiguration<T> config;
	private Deque<T> mappeds = null;
	
	/**
	 * Constructor 
	 * 
	 * @param mappedType the type of the mapped value Object(s) 
	 * wrapped by the MappingWrapper to be instantiated 
	 */
	public MappingWrapper(Class<T> mappedType){
		this.config = new MappingConfiguration<T>(mappedType);
		this.mappeds = new LinkedList<>();
		if(!this.config.getPrefix().exists()) {
			newMappedInstance();			
		}
	}

	/**
	 * Constructor
	 * 
	 * @param mapped the initial mapped value Object wrapped by the 
	 * MappingWrapper to be instantiated
	 */
	public MappingWrapper(T mapped){
		if(mapped == null) {
			throw new NullPointerException("Unable to create a new instance of the mapped type");
		}
		this.config = new MappingConfiguration<T>((Class<T>) mapped.getClass());
		this.mappeds = new LinkedList<>();
		if(!this.config.getPrefix().exists()) {
			this.mappeds.addFirst(mapped);
		}
	}

	/**
	 * Constructor
	 */
	public MappingWrapper(){
		this.config = new MappingConfiguration<T>();
		this.mappeds = new LinkedList<>();
		newMappedInstance();
	}
	
	/**
	 * Returns the {@link MappingConfiguration} of this MappingWrapper
	 * 
	 * @return this MappingWrapper's {@link MappingConfiguration}
	 */
	public MappingConfiguration<T> getMappingConfiguration() {
		return this.config;
	}
	
	/**
	 * Returns the mapping result object that can be a single 
	 * instance of the mapped Type of this MappingWrapper, or 
	 * a List of instances of the mapped type, if this last one
	 * has been annotated with a {@link cmssi.lyson.annotation.LysonMapping} 
	 * Annotation
	 * 
	 * @return mapping result Object
	 */
	public Object get() {
		if(this.config.getMappedType() == null){
			return ((List<?>)this.mappeds.getFirst()).get(0);
		}
		if(this.config.getPrefix().exists()) {
			return this.mappeds;
		}
		return this.mappeds.getFirst();
	}	
	
	/**
	 * Returns the currently mapped Object
	 *  
	 * @return the mapped Object
	 */
	public Object mapped() {
		return this.mappeds.getFirst();
	}
		
	/**
	 * Creates a new instance of the mapped type of this MappingWrapper
	 * and adds it to the List of already instantiating ones
	 */
	protected void newMappedInstance() {
		T mapped = null;
		Class<T> mappedType = this.config.getMappedType();
		if(mappedType == null) {
			mapped =  (T) new ArrayList<Object>();
		} else {
			try {
				mapped = mappedType.getConstructor().newInstance();		
			} catch(ReflectiveOperationException e) {
				if(LOG.isLoggable(Level.SEVERE)) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
		if(mapped == null) {
			throw new NullPointerException(String.format(
			"Unable to create a new instance of the mapped type :%s",mappedType.getName()));
		}
		mappeds.addFirst(mapped);
	}
	
}
