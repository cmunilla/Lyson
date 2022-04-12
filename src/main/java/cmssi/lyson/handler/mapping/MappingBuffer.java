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
package cmssi.lyson.handler.mapping;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Deque;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MappingBuffer buffers the mapped Objects during the mapping process
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class MappingBuffer {
	
	private static final Logger LOG = Logger.getLogger(MappingBuffer.class.getName());
	
	private MappingConfiguration config;
	private Deque<Object> mappeds = null;	

	/**
	 * Constructor 
	 * 
	 * @param mappedType the Java Type of the mapped value Object(s) 
	 * wrapped by the MappingBuffer to be instantiated 
	 */
	public MappingBuffer(Class<?> mappedType) {
		this(new MappingType(mappedType));
	}
	
	/**
	 * Constructor 
	 * 
	 * @param mappedType the {@link MappingType} wrapping the Java Type of the mapped 
	 * value Object(s) wrapped by the MappingBuffer to be instantiated 
	 */
	public MappingBuffer(MappingType mappedType) {
		this.config = new MappingConfiguration(mappedType);
		this.mappeds = new LinkedList<>();
		if(!this.config.getPrefix().exists()) 
			newMappedInstance();			
	}	

	/**
	 * Constructor
	 * 
	 * @param mapped the initial mapped value Object wrapped by the 
	 * MappingBuffer to be instantiated
	 */
	public MappingBuffer(Object mapped){
		if(mapped == null) 
			throw new NullPointerException("Unable to create a new instance of the mapped type");
		this.config = new MappingConfiguration(new MappingType(mapped.getClass()));
		this.mappeds = new LinkedList<>();
		if(!this.config.getPrefix().exists()) 
			this.mappeds.addFirst(mapped);
	}

	/**
	 * Constructor
	 * 
	 * @param handleIdentity defines whether JSON Object item key might be reused to assign
	 * as identity to newly created complex data structure (Map, List) - For the specific 
	 * case of List, the identity field will be the first List entry 
	 */
	public MappingBuffer(boolean handleIdentity){
		this.config = new MappingConfiguration(handleIdentity);
		this.mappeds = new LinkedList<>();
		newMappedInstance();
	}
	
	/**
	 * Constructor
	 */
	public MappingBuffer(){
		this(false);
	}
		
	/**
	 * Returns the mapping result object. It can be a single instance of the mapped type, or a List 
	 * of instances if the mapped type has been annotated with a {@link cmssi.lyson.annotation.LysonMapping} 
	 * Annotation defining a prefix.
	 * 
	 * @return the mapping result object
	 */
	public Object collect() {
		if(config.getMappingBuilder().isPojoCollection()) {
			Object container = this.mappeds.getLast();
			for(Iterator it = this.mappeds.descendingIterator();it.hasNext();) {
				Object obj = it.next();
				if(obj == container)
					continue;
				if(Collection.class.isAssignableFrom(container.getClass())) 
					((Collection)container).add(obj);	
				else {
					AccessibleObject ao = config.getMapping(MappingConfiguration.IDENTITY_MAPPING);					
					String identityName = null;				
					if(ao == null){
						if(LOG.isLoggable(Level.SEVERE)) 
							LOG.log(Level.SEVERE, "Unable to find identity field name");
						continue;
					}
					if(ao instanceof Field) 	    			
						identityName = ((Field)ao).getName();
		    		else if(ao instanceof Method)
		    			identityName = MappingConfiguration.fieldNameFromSetterName(((Method)ao).getName());
		    		try {
						Field identityField = obj.getClass().getDeclaredField(identityName);
						identityField.setAccessible(true);
						String identity = String.valueOf(identityField.get(obj));
						if(Dictionary.class.isAssignableFrom(container.getClass()))
							((Dictionary)container).put(identity, obj);
						else if(Map.class.isAssignableFrom(container.getClass()))
							((Map)container).put(identity, obj);
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
						if(LOG.isLoggable(Level.SEVERE)) 
							LOG.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}	
			if(config.getMappingBuilder().isArrayType()) 
				container = this.config.getMappingBuilder().asArray((Collection)container);			
			return container;
		}
		if(this.config.getMappingBuilder().getMappedTypeName() == null)
			return ((List)this.mappeds.getFirst()).get(0);
		if(config.getMappingBuilder().isArrayType()) 
			return this.config.getMappingBuilder().asArray((Collection)this.mappeds.getFirst());	
		if(this.config.getPrefix().exists())
			return this.mappeds;
		return this.mappeds.getFirst();
	}	
	
	/**
	 * Returns the last created instance of the mapped object type
	 *  
	 * @return the last created instance
	 */
	public Object getCurrent() {
		return this.mappeds.getFirst();
	}	

	/**
	 * Returns the {@link MappingConfiguration} of this MappingBuffer
	 * 
	 * @return this MappingBuffer's {@link MappingConfiguration}
	 */
	public MappingConfiguration getMappingConfiguration() {
		return this.config;
	}

	/**
	 * Returns true if an JSON Object item event key might be reused to assign as identity to 
	 * newly created complex data structure (Map, Dictionary, POJO). Returns false otherwise
	 * 
	 * @return true if an JSON Object item event key might be reused to assign as identity; 
	 * returns false otherwise
	 */
	public boolean handleIdentity() {
		return this.config.handleIdentity();
	}	
	
	/**
	 * Creates a new instance of the mapped type and adds it to the List of already 
	 * instantiated ones
	 */
	protected void newMappedInstance() {
		Object mapped = null;
		if(config.getMappingBuilder().isPojoCollection() && this.mappeds.isEmpty()) 
			mapped = this.config.getMappingBuilder().buildMappedObjectsContainer();
		else
			mapped = this.config.getMappingBuilder().buildMappedObject();		
		if(mapped == null)
			throw new NullPointerException(String.format("Unable to create a new instance of the mapped type :%s", 
					this.config.getMappingBuilder().getMappedTypeName()));
		this.mappeds.addFirst(mapped);
	}
}
