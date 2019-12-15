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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.annotation.LysonMapping;
import cmssi.lyson.event.KeyValueEventWrapper;
import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.event.ValuableEventWrapper;
import cmssi.lyson.exception.LysonParsingException;

/**
 * {@link LysonParserHandler} implementation dedicated to a JSON chars sequence mapping
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.2
 */
public class MappingHandler implements LysonParserHandler {

	private static final Logger LOG = Logger.getLogger(MappingHandler.class.getName());
	
	private final Map<String, AccessibleObject> mapping;	

	//keep intermediate data structure while parsing
	private Stack<Object> stack = new Stack<>();
	
	private final boolean undefined;
	private Object mapped;

	/**
	 * Constructor
	 * 
	 * @param mappedType the java type to map the parsed JSON chars sequence to
	 */
	public MappingHandler(Class<?> mappedType){
		try {
			mapped = mappedType.getConstructor().newInstance();		
		} catch(ReflectiveOperationException e) {
			if(LOG.isLoggable(Level.SEVERE)) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		if(mapped == null) {
			throw new NullPointerException("Unable to create a new instance of the mapped type");
		}	
		undefined = false;
		this.mapping = Collections.synchronizedMap(new HashMap<>());
		buildMapping(mappedType);	
	}
	
	/**
	 * Constructor
	 * 
	 * @param mapped the instance to which assign the fields of with the parsed 
	 * JSON chars sequence
	 */
	public MappingHandler(Object mapped){				
		if(mapped == null) {
			throw new NullPointerException("Null mapped argument");
		}
		this.mapped = mapped;	
		this.mapping = Collections.synchronizedMap(new HashMap<>());
		undefined = false;
		buildMapping(mapped.getClass());
	}
	
	/**
	 * Constructor
	 */
	//If no mapped type or instance is provided the JSON chars sequence will be 
	//converted into Map or a List
	public MappingHandler(){
		//the mapping result will be the single element of the mapped List
		this.mapped = new ArrayList<Object>();
		undefined = true;
		this.mapping = Collections.synchronizedMap(new HashMap<>());
		this.stack.add(mapped);
	}
	
	//build the Map whose key field is the name or the path of the targeted 
	//LysonParsingEvent and whose value field is the primitive or the JSON 
	//data structure attached to this last one
	private void buildMapping(Class<?> target) {		
		Set<AccessibleObject> accessibles = new HashSet<>();
		accessibles.addAll(Arrays.asList(target.getDeclaredFields()));
		accessibles.addAll(Arrays.asList(target.getDeclaredMethods()));
		accessibles.stream().forEach(f -> {
		    LysonMapping lm = f.getAnnotation(LysonMapping.class);	
		    if(lm != null) {
		    	String mappingName = lm.mapping();
		    	if(mappingName.length() == 0) {
		    		try {
			    		mappingName = null;
			    		switch(f.getClass().getSimpleName()) {
		    			case "Field": 
		    				mappingName = ((Field)f).getName();
		    				break;
		    			case "Method": 
		    				//method is supposed to be a setter whose name is 
		    				//compliant to pattern : (set)([A-Z][a-z]+) where the 
		    				//second group is the name of the field with a first 
		    				//uppercase letter
		    				mappingName = fieldNameFromSetterName(((Method)f).getName());			    				
		    			default:
		    				break;
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
		    }
		});
	}	
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see cmssi.lyson.handler.LysonParserHandler.handle(cmssi.lyson.event.ParsingEvent);
	 */
	@Override
	public boolean handle(ParsingEvent event) {
		if(event == null) {
			return false;
		}
		Object val = null;
		String key =  event.getPath();
		AccessibleObject ao = mapping.get(key);
		
		if(ao == null) {
			KeyValueEventWrapper wrapper = event.adapt(KeyValueEventWrapper.class);
			if(wrapper!=null) {
				key = wrapper.getKey();
				ao = mapping.get(key);
			}
		}
		switch(event.getType()) {
			case ParsingEvent.JSON_ARRAY_OPENING:
				val = new ArrayList<Object>();
				break;
			case ParsingEvent.JSON_OBJECT_OPENING:
				val = new HashMap<String,Object>();
				break;
			case ParsingEvent.JSON_ARRAY_ITEM:
				ValuableEventWrapper vwrapper = event.adapt(ValuableEventWrapper.class);
				if(vwrapper!=null) {
					val = vwrapper.getValue();
				}
				break;
			case ParsingEvent.JSON_OBJECT_ITEM:
				KeyValueEventWrapper kvw = event.adapt(KeyValueEventWrapper.class);
				if(kvw!=null) {
					key = kvw.getKey();
					val = kvw.getValue();
				}
				break;
			case ParsingEvent.JSON_OBJECT_CLOSING:
			case ParsingEvent.JSON_ARRAY_CLOSING:
				if(!stack.isEmpty()) {
					stack.pop();
				}
				return true;
			default:
				break;
		}
		if(!stack.isEmpty()) {
			Object coll = stack.peek();
			switch(coll.getClass().getSimpleName()) {
			case "ArrayList" :
				((List)coll).add(val);	
				break;
			case "HashMap":
				((Map)coll).put(key , val);	
			}
		}
		if(ao != null){
			try {
				ao.setAccessible(true);
				switch(ao.getClass().getSimpleName()) {
				case "Field" :
					((Field)ao).set(mapped , val);	
					break;
				case "Method":
					((Method)ao).invoke(mapped , val);	
				}
			} catch (IllegalArgumentException 
				   | IllegalAccessException 
				   | InvocationTargetException e) {
    			if(LOG.isLoggable(Level.SEVERE)) {
    				LOG.log(Level.SEVERE,e.getMessage(),e);
    			}
    			return false;
			}
		}
		switch(event.getType()) {
			case ParsingEvent.JSON_ARRAY_OPENING:
			case ParsingEvent.JSON_OBJECT_OPENING:
				if(ao!=null || !stack.isEmpty()) {
					stack.push(val);
				}
				break;
			case ParsingEvent.JSON_ARRAY_ITEM:
			case ParsingEvent.JSON_OBJECT_ITEM:
			case ParsingEvent.JSON_OBJECT_CLOSING:
			case ParsingEvent.JSON_ARRAY_CLOSING:
			default:
				break;
		}		
		if(LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST,event.toString());
		}
	    return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cmssi.lyson.handler.LysonParserHandler.handle(cmssi.lyson.exception.LysonParsingException);
	 */
	@Override
	public void handle(LysonParsingException parsingException) {
		if(LOG.isLoggable(Level.SEVERE)) {
			LOG.log(Level.SEVERE,parsingException.getMessage(),parsingException);
		}
	}
	
	/**
	 * Returns the &lt;T&gt; typed mapped instance
	 * 
	 * @param <T> the expected 
	 * 
	 * @return the &lt;T&gt; typed mapped instance
	 */
	public <T> T getMapped() {
		if(undefined){
			return (T) ((List)mapped).get(0);
		}
		return (T) this.mapped;
	}
	
	//retrieve the targeted field name from the setter method name
	private static String fieldNameFromSetterName(String methodName) {
		String fieldName = methodName;
		//just translate uppercase letter to lowercase by adding 32
		char c = (char)(((int)(fieldName.charAt(3)) + 32));
		fieldName = new StringBuilder().append(c).append(fieldName.substring(4)).toString();
		return fieldName;
	}
}
