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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.event.KeyValueEventWrapper;
import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.event.ValuableEventWrapper;
import cmssi.lyson.exception.LysonParsingException;

/**
 * {@link LysonParserHandler} implementation dedicated to a JSON chars sequence mapping
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.3
 */
public class MappingHandler<T> implements LysonParserHandler {

	private static final Logger LOG = Logger.getLogger(MappingHandler.class.getName());

	//keep intermediate data structure while parsing
	private Deque<Object> stack = new LinkedList<>();
	private MappingWrapper<T> wrapper;
	
	
	/**
	 * Constructor
	 * 
	 * @param mappedType the java type to map the parsed JSON chars sequence to
	 */
	public MappingHandler(Class<T> mappedType){
		this.wrapper = new MappingWrapper<T>(mappedType);
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param mapped the instance to which assign the fields of with the parsed 
	 * JSON chars sequence
	 */
	public MappingHandler(T mapped){
		this.wrapper = new MappingWrapper<T>(mapped);
	}
	
	/**
	 * Constructor
	 */
	//If no mapped type or instance is provided the JSON chars sequence will be 
	//converted into Map or a List
	public MappingHandler(){
		this.wrapper = new MappingWrapper<T>();
		this.stack.add(this.wrapper.mapped());
	}
		
	@Override
	public boolean handle(ParsingEvent event) {
		if(event == null) {
			return false;
		}		
		if(LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST,event.toString());
		}
		Object val = null;
		MappingConfiguration<T> config = this.wrapper.getMappingConfiguration();
		String key =  config.getPrefix().getSuffix(event.getPath());		
				
		AccessibleObject ao = config.getMapping(key);
		
		if(ao == null) {
			KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
			if(kvwrapper!=null) {
				key = config.getPrefix().getSuffix(kvwrapper.getKey());
				ao = config.getMapping(key);
			}
		}
		switch(event.getType()) {
			case ParsingEvent.JSON_ARRAY_OPENING:
				if(config.getPrefix().isPrefix(event.getPath())) {
					this.wrapper.newMappedInstance();
				}
				val = handleJsonOpening(ao,ArrayList.class);
				break;
			case ParsingEvent.JSON_OBJECT_OPENING:
				if(config.getPrefix().isPrefix(event.getPath())) {
					this.wrapper.newMappedInstance();					
				}
				val = handleJsonOpening(ao,HashMap.class);
				break;
			case ParsingEvent.JSON_ARRAY_ITEM:
				ValuableEventWrapper vwrapper = event.adapt(ValuableEventWrapper.class);
				if(vwrapper!=null) {
					val = vwrapper.getValue();
				}
				assignValue(ao,val);
				break;
			case ParsingEvent.JSON_OBJECT_ITEM:
				KeyValueEventWrapper kvw = event.adapt(KeyValueEventWrapper.class);
				if(kvw!=null) {
					key = kvw.getKey();
					val = kvw.getValue();
				}		
				assignValue(ao,val);
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
			Object obj = stack.peek();
			if(obj instanceof List) {
				((List)obj).add(val);
			} else if(obj instanceof Map){
				((Map)obj).put(key , val);	
			} else if(obj instanceof MappingHandler) {				
				((MappingHandler)obj).handle(event);
				return true;
			}
		}
		if((ao!=null || !stack.isEmpty()) 
		&& (event.getType()==ParsingEvent.JSON_ARRAY_OPENING ||event.getType()==ParsingEvent.JSON_OBJECT_OPENING)) {
			stack.push(val);
		}
	    return true;
	}

	@Override
	public void handle(LysonParsingException parsingException) {
		if(LOG.isLoggable(Level.SEVERE)) {
			LOG.log(Level.SEVERE,parsingException.getMessage(),parsingException);
		}
	}
	
	/**
	 * Returns the &lt;K&gt; typed mapped instance
	 * 
	 * @param <K> the mapping object result type
	 *  
	 * @return the &lt;K&gt; typed mapped instance
	 */
	@SuppressWarnings("unchecked")
	public <K> K getMapped() {
		return (K) this.wrapper.get();
	}
	
	//According to the type of the JSON data structure opening event, and the type 
	//of the Field of the mapped Object targeted by the ao AccessibleObject argument
	//the assigned value is a Map, a List or the mapped Object of a sub MappingHandler
	private Object handleJsonOpening(AccessibleObject ao, Class<?> defaultValueType) {
		Object val = null;
		Object stacked = null;
		Object defaultValue = null;
		try {
			defaultValue = defaultValueType.getConstructor().newInstance();	
		} catch(ReflectiveOperationException e) {
			if(LOG.isLoggable(Level.SEVERE)) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}		
		if(ao == null) {
			val = defaultValue;
			stacked = defaultValue;
		} else {
			Class<?> type = null;
			if(ao instanceof Field) {
				type = ((Field)ao).getType();	
			} else if(ao instanceof Method) {
				type = ((Method)ao).getParameterTypes()[0];	
			}
			if(type.isAssignableFrom(defaultValueType)) {
				val = defaultValue;
				stacked = defaultValue;
			} else {
				try {
					MappingHandler sub = new MappingHandler(type);
					val = sub.getMapped();	
					stacked = sub;
				} catch(NullPointerException e) {
					if(LOG.isLoggable(Level.SEVERE)){
						LOG.log(Level.SEVERE,e.getMessage(),e);
					}
				}
			}
		}
		assignValue(ao,val);
		return stacked;
	}
	
	//Define the val value Object of the appropriate field of 
	//the mapped object using the ao AccessibleObject argument  
	//by setting it if it's a Field or invoking it if it's a
	//Method
	private void assignValue(AccessibleObject ao, Object val) {
		if(ao == null) {
			return;
		}			
		try {
			ao.setAccessible(true);
			if(ao instanceof Field) {
				((Field)ao).set(this.wrapper.mapped() , val);	
			}else if(ao instanceof Method) {
				((Method)ao).invoke(this.wrapper.mapped() , val);	
			}
		} catch (IllegalArgumentException 
			   | IllegalAccessException 
			   | InvocationTargetException e) {
			if(LOG.isLoggable(Level.SEVERE)) {
				LOG.log(Level.SEVERE,e.getMessage(),e);
			}
		}
	}
}
