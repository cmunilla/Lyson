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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.annotation.LysonMapping;

/**
 * Configuration of a mapping process
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.4
 */
public class MappingConfiguration<T> {
	
	private static final Logger LOG = Logger.getLogger(MappingConfiguration.class.getName());

	//retrieve the targeted field name from the setter method name
	private static String fieldNameFromSetterName(String methodName) {
		String fieldName = methodName;
		//just translate uppercase letter to lowercase by adding 32
		char c = (char)(((int)(fieldName.charAt(3)) + 32));
		fieldName = new StringBuilder().append(c).append(fieldName.substring(4)).toString();
		return fieldName;
	}
	
	private Map<String, AccessibleObject> mapping;	
	
	private Class<T> mappedType = null;
	
	private MappingPrefix prefix;
	
	
	/**
	 * Constructor 
	 * 
	 * @param mappedType the type of the mapped value Object(s) 
	 * wrapped by the MappingWrapper to be instantiated 
	 */
	public MappingConfiguration(Class<T> mappedType){
		this.mappedType = mappedType;
		this.prefix = new MappingPrefix(mappedType);
		this.mapping = Collections.synchronizedMap(new HashMap<>());
		buildAnnotatedMapping();		
		if(this.mapping.isEmpty()) {
			buildRawMapping();
		}
	}

	/**
	 * Constructor
	 */
	public MappingConfiguration(){
		this.prefix = new MappingPrefix();
		this.mapping = Collections.synchronizedMap(Collections.emptyMap());
	}
	
	/**
	 * Returns the {@link AccessibleObject} mapped to the String 
	 * key passed as parameter 
	 * 
	 * @param mapping the String key for which retrieving the mapped
	 * {@link AccessibleObject} if any
	 * 
	 * @return the {@link AccessibleObject} mapped to the specified
	 * String key if any - Null otherwise
	 */
	public AccessibleObject getMapping(String mapping) {
		return this.mapping.get(mapping);
	}

	/**
	 * Returns the defined mapped Type of the mapping process
	 * configured by this MappingConfiguration
	 * 
	 * @return this MappingConfiguration's mapped Type
	 */
	public Class<T> getMappedType() {
		return this.mappedType;
	}
	
	/**
	 * Returns the {@link MappingPrefix} of this MappingConfiguration
	 * 
	 * @return this MappingConfiguration's {@link MappingPrefix}
	 */
	public MappingPrefix getPrefix() {
		return this.prefix;
	}	
	
	//Using @LysonMapping annotated fields and methods, build the Map whose key field is 
	//the name or the path of the targeted LysonParsingEvent and whose value field is the 
	//primitive or the JSON data structure attached to this last one
	private void buildAnnotatedMapping() {	
		Set<AccessibleObject> accessibles = new HashSet<>();
		accessibles.addAll(Arrays.asList(this.mappedType.getDeclaredFields()));
		accessibles.addAll(Arrays.asList(this.mappedType.getDeclaredMethods()));
		accessibles.stream().forEach(f -> {
		    LysonMapping lm = f.getAnnotation(LysonMapping.class);	
		    if(lm == null) {
		    	return;
		    }
	    	String mappingName = lm.mapping();
	    	if(mappingName.length() == 0) {
	    		try {
		    		mappingName = null;
		    		if(f instanceof Field) {		    			
	    				mappingName = ((Field)f).getName();
		    		} else if(f instanceof Method) {		    			
	    				//method is supposed to be a setter whose name is 
	    				//compliant to pattern : (set)([A-Z][a-z]+) where the 
	    				//second group is the name of the field with a first 
	    				//uppercase letter
	    				mappingName = fieldNameFromSetterName(((Method)f).getName());
		    		}
	    		} catch(SecurityException e){
	    			if(LOG.isLoggable(Level.SEVERE)) {
	    				LOG.log(Level.SEVERE,e.getMessage(),e);
	    			}
	    		}
	    	}
	    	if(mappingName != null) {
	    		mapping.put(mappingName, f);
	    	}
		});
	}

	//Using all the fields, build the Map whose key field is the name or the path of 
	//the targeted LysonParsingEvent and whose value field is the primitive or the JSON 
	//data structure attached to this last one
	private void buildRawMapping() {
		Arrays.stream(this.mappedType.getDeclaredFields()).forEach(f -> {
	    	mapping.put(f.getName(), f);
		});
	}
}
