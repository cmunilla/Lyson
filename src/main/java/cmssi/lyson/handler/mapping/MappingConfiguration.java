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
package cmssi.lyson.handler.mapping;

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
 * @version 0.5
 */
public class MappingConfiguration {
	
	private static final Logger LOG = Logger.getLogger(MappingConfiguration.class.getName());

	public static final String IDENTITY_MAPPING = "#IDENTITY#";
	
	//retrieve the targeted field name from the setter method name
	protected static String fieldNameFromSetterName(String methodName) {
		String fieldName = methodName;
		//just translate uppercase letter to lowercase by adding 32
		char c = (char)(((int)(fieldName.charAt(3)) + 32));
		fieldName = new StringBuilder().append(c).append(fieldName.substring(4)).toString();
		return fieldName;
	}
	
	private Map<String, AccessibleObject> mapping;		
	private MappingPrefix prefix;
	
	private boolean handleIdentity = false;	
	private MappingType mappingType = null;
	private MappingBuilder mappingBuilder = null;
	
	/**
	 * Constructor 
	 * 
	 * @param mappingType the {@link MappingType} wrapping the Java Type to be 
	 * used in the mapping process
	 */
	public MappingConfiguration(MappingType mappingType){
		this.mappingType = mappingType;
		if(this.mappingType.isPojoCollection())
			this.prefix = new MappingPrefix(this.mappingType.getComponentType());
		else
			this.prefix = new MappingPrefix(this.mappingType.getMappedType());			
		this.mapping = Collections.synchronizedMap(new HashMap<>());
		buildAnnotatedMapping();		
		if(this.mapping.isEmpty()) 
			buildRawMapping();	
		this.mappingBuilder = new MappingBuilder(this.mappingType);
	}
	
	/**
	 * Constructor
	 * 
	 * @param handleIdentity defines whether JSON Object item key might be reused to be assigned
	 * as identity to newly created complex data structure  - For the specific case of a List, the 
	 * identity field will be defined as the first item
	 */
	public MappingConfiguration(boolean handleIdentity){
		this.mappingType = new MappingType(null);
		this.prefix = new MappingPrefix(this.mappingType.getMappedType());		
		this.mapping = Collections.synchronizedMap(Collections.emptyMap());
		this.handleIdentity = handleIdentity;
		this.mappingBuilder = new MappingBuilder(this.mappingType);
	}
	
	/**
	 * Constructor
	 */
	public MappingConfiguration(){
		this(false);
	}

	/**
	 * Returns the {@link AccessibleObject} mapped to the String key passed as parameter 
	 * 
	 * @param mapping the String key for which retrieving the mapped {@link AccessibleObject} if any
	 * 
	 * @return the {@link AccessibleObject} mapped to the specified String key if any - Null otherwise
	 */
	public AccessibleObject getMapping(String mapping) {
		return this.mapping.get(mapping);
	}

	/**
	 * Returns the {@link MappingBuilder} of this {@link MappingConfiguration}
	 * 
	 * @return this {@link MappingConfiguration}'s {@link MappingBuilder}
	 */
	public MappingBuilder getMappingBuilder() {
		return this.mappingBuilder;
	}
	
	/**
	 * Returns the {@link MappingPrefix} of this {@link MappingConfiguration}
	 * 
	 * @return this {@link MappingConfiguration}'s {@link MappingPrefix}
	 */
	public MappingPrefix getPrefix() {
		return this.prefix;
	}	
	
	/**
	 * Returns true if an JSON Object item event key might be reused to be assigned as identity to 
	 * newly created complex data structure. Returns false otherwise
	 * 
	 * @return true if an JSON Object item event'key might be reused to be assigned as an identity
	 * field; returns false otherwise
	 */
	public boolean handleIdentity() {
		return this.handleIdentity;
	}

	//Using @LysonMapping annotated fields and methods, build the Map whose key field is the name or 
	//the path of the targeted LysonParsingEvent and whose value field is the AccessibleObject (Field
	//or Method) in the used mapping type
	private void buildAnnotatedMapping() {	
		Class<?> annotatedClass = this.mappingType.isPojoCollection()?this.mappingType.getComponentType():this.mappingType.getMappedType();
		LysonMapping typemapping = annotatedClass.getAnnotation(LysonMapping.class);
		boolean implicit = typemapping!=null?typemapping.implicit():false;

		Set<AccessibleObject> accessibles = new HashSet<>();
		accessibles.addAll(Arrays.asList(annotatedClass.getDeclaredFields()));
		accessibles.addAll(Arrays.asList(annotatedClass.getDeclaredMethods()));
		accessibles.stream().forEach(f -> {
		    LysonMapping lm = f.getAnnotation(LysonMapping.class);	
		    if(!implicit && lm == null) 
		    	return;
		    
	    	String mappingName = lm!=null?lm.mapping():null;
	    	if(mappingName==null || mappingName.length() == 0) {
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
	    		if(!this.handleIdentity)
	    			this.handleIdentity = IDENTITY_MAPPING.equals(mappingName);
	    		this.mapping.put(mappingName, f);
	    	}
		});
	}

	//Using all the fields, build the Map whose key field is the name or the path of 
	//the targeted LysonParsingEvent and whose value field is the AccessibleObject (Field
	//or Method) in the used mapping type
	private void buildRawMapping() {
		Class<?> targetClass = this.mappingType.isPojoCollection()?this.mappingType.getComponentType():this.mappingType.getMappedType();
		Arrays.stream(targetClass.getDeclaredFields()).forEach(f -> {
	    	mapping.put(f.getName(), f);
		});
	}
}
